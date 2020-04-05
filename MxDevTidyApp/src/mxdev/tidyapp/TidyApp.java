/*
 * Copyright (c) 2020 Bruno Portaluri (MaximoDev)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package mxdev.tidyapp;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;


/**
 * Parses the XML structure of a Maximo application and detects which fields are never used.
 * We assume all database columns with only one value can be removed.
 * - Always null (user never entered a value)
 * - User never changed the default value
 */
public class TidyApp
{
	static String confDbDriver;
	static String confDbUrl;
	static String confUser;
	static String confPwd;
    static String confLoglevel;

    static String inFileName;
    static String outFileName;

	static String rootMboName;
    static String rootResultstableid;
	
    static int fieldCount;
    static int fieldUnusedCount;
	
	static MxDD dd;
	static List<Element> toBeRemoved = new ArrayList<Element>();
	
	static Connection conn = null;
	static Statement statement = null;
	static PreparedStatement preparedStatement = null;
	static ResultSet resultSet = null;
	

	public static void main(String[] args) throws Exception
	{
		if (args.length!=1)
		{
			log("Missing argument: input file");
			log("");
			return;
		}
		
		log("Starting MaximoDev TidyApp");
		
		//log("Reading configuration");

        Properties props = new Properties();
        FileInputStream fis = new FileInputStream("MxDevTidyApp.properties");
        props.load(fis);

    	confDbDriver = props.getProperty("mxe.db.driver");
    	confDbUrl = props.getProperty("mxe.db.url");
    	confUser = props.getProperty("mxe.db.user");
    	confPwd = props.getProperty("mxe.db.password");
        confLoglevel = props.getProperty("loglevel");

        log("Connecting to Maximo database: " + confDbUrl);
        
		Class.forName(confDbDriver);
		conn = DriverManager.getConnection(confDbUrl, confUser, confPwd);
		
		logInfo("Loading Maximo data dictionary");
		
		dd = new MxDD();
		dd.init(conn);

		inFileName = args[0];
		outFileName = inFileName.replace(".xml", ".new.xml");
		log("Processing input file: " + inFileName);		

		SAXBuilder builder = new SAXBuilder();
		File xmlFile = new File(args[0]);
		
		Document doc = (Document) builder.build(xmlFile);
		Element rootNode = doc.getRootElement();
		
		rootMboName = rootNode.getAttribute("mboname").getValue();
		rootResultstableid = rootNode.getAttribute("resultstableid").getValue();
		
		checkChildren(rootNode, 0, rootMboName, rootMboName);
		
		//for (Element element:toBeRemoved)
		//{
		//	element.getParent().removeContent(element);
		//}

		logInfo("Generating output file: " + outFileName);
		
		XMLOutputter xmlOutput = new XMLOutputter();
		xmlOutput.setFormat(Format.getPrettyFormat());
		xmlOutput.output(doc, new FileWriter(outFileName));

		if(!conn.getAutoCommit())
			conn.commit();
		
		conn.close();
		
		logInfo("");
		log("MxDevTidyApp ended successfully");
		log("Analyzed fields: " + fieldCount);
		log("Unused fields: " + fieldUnusedCount + " (" + (fieldUnusedCount*100/fieldCount) +"%)");
		log("Tidied app generated: " + outFileName);
	}
	
	/**
	 * This is the main recursive method
	 * Outline of the logic
	 * 
	 * Get all the child XML elements
	 * For each element
	 *   If it is a block (section/table/tab/etc.)
	 *     If there is a relationship/datasrc attribute
	 *       Determine the MBO on which this block is based
	 *   If it is a field (textbox/checkbox/etc.)
	 *     Query the database for distinct values to detect if the field is not used
	 */
	static void checkChildren(Element rootNode, int level, String mboPath, String mboName) throws SQLException
	{
		List<Element> childElements = rootNode.getChildren();
		
		for(int i=0; i<childElements.size(); i++)
		{
			Element elem = childElements.get(i);
			String elName= elem.getName();
			String elId= elem.getAttributeValue("id");

			if ("|tab|page|clientarea|tabgroup|tabledetails|sectionrow|sectioncol|table|section|".contains("|"+elName+"|"))
			{
				//logDebug(elName + ": " + elId, level);

				String childMboPath=mboPath;
				String childMbo=mboName;

				if (elem.getAttribute("relationship")!=null)
				{
					String relName = elem.getAttributeValue("relationship").toUpperCase();

					childMboPath = childMboPath + "." + relName;
					childMbo = dd.getRelTable(mboName, relName);
				}
				else if (elem.getAttribute("datasrc")!=null)
				{
					String relName = elem.getAttributeValue("datasrc").toUpperCase();
					
					if(!relName.equalsIgnoreCase(rootResultstableid))
					{
						childMboPath = childMboPath + "." + relName;
						childMbo = dd.getRelTable(rootMboName, relName);
					}
				}
				
				logDebug(elName + ": " + elId + " - " + childMboPath + " >>>> " + childMbo, level);
				checkChildren(elem, level+1, childMboPath, childMbo);
				if (elem.getChildren().size() == 0)
				{
					elem.getParent().removeContent(elem);
					
					// we removed the current child element so we have to stay on the current record
					i--;
				}
			}
			else if ("|tablecol|textbox|multiparttextbox|checkbox|".contains("|"+elName+"|"))
			{
				Attribute da = elem.getAttribute("dataattribute");
				if (da!=null)
				{
					String attrname=da.getValue().toUpperCase();
					String tablename=mboName;
					
					// sometime textbox control is linked to a specific datasrc
					if(elem.getAttribute("datasrc")!=null)
					{
						if(elem.getAttribute("datasrc").getValue().toUpperCase().equals("MAINRECORD"))
							tablename=rootMboName;
					}
					
					// if dataattribute contains a '.' we have to navigate relationship
					if(attrname.contains("."))
					{
						String[] x = attrname.split("\\.");
						tablename = dd.getRelTable(tablename, x[0]);
						attrname = x[1];
					}
					
					logDebug(elName + ": " + elId + " - " + tablename + "." + attrname, level);
				    fieldCount++;
				    
					// call the isDbFieldUsed to detect if a field is used querying the database
				    if (isDbFieldUsed(tablename, attrname))
					{
						logInfo(elName + ": " + elId + " - " + tablename + "." + attrname + " >>>> UNUSED", level);
						//toBeRemoved.add(elem);
						elem.getParent().removeContent(elem);
						fieldUnusedCount++;
						
						// we removed the current child element so we have to stay on the current record
						i--;
					}
				}
			}
			
			// TODO data sources can be defined after they are used
			// they should be collected in a 1st pass of the XML
			if ("|datasrc|table|".contains("|"+elName+"|"))
			{
				Attribute rel = elem.getAttribute("relationship");
				if (rel!=null)
				{
					String tb = dd.addRel(rootMboName, elId.toUpperCase(), elem.getAttributeValue("relationship"));
					logInfo("Adding datasrc to DD " + elName + ": " + elId + " >>>> " + rootMboName + "." + elId.toUpperCase() + " >>> " + tb, level);
				}
			}
		}
	}
	
	
	static boolean isDbFieldUsed(String table, String attr) throws SQLException
	{
		if (!dd.isPersistent(table, attr))
			return false;

		String sqlQuery = "select count(distinct("+attr+")) from " + table;
		
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(sqlQuery);
	 
	    rs.next();
	    int i = rs.getInt(1);
	    rs.close();
	    stmt.close();

	    if (i<=1)
	    	return true;
	    else
	    	return false;
	}
	
	
	////////////////////////////////
	// Logging functions
	////////////////////////////////
	
	static void log(String msg)
	{
		System.out.println(msg);
	}
	
	static void logInfo(String msg, int indent)
	{
    	if (confLoglevel.equals("DEBUG"))
    	{
			for(int x=0; x<indent; x++)
				System.out.print("  ");
			System.out.println(msg);
    	}
	}
	
	static void logInfo(String msg)
	{
		logInfo(msg, 0);
	}
	
	static void logDebug(String msg, int indent)
    {
    	if (confLoglevel.equals("DEBUG") || confLoglevel.equals("INFO"))
    	{
    		for(int x=0; x<indent; x++)
    			System.out.print("  ");
    		System.out.println(msg);    		
    	}
    }
	
	static void logDebug(String msg)
    {
   		logDebug(msg, 0);
    }
}
