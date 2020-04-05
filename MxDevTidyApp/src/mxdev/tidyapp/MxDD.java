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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;


public class MxDD
{
	HashMap<String, String> attrs = new HashMap<String, String>();
	HashMap<String, String> rels = new HashMap<String, String>();
	
	void init(Connection conn) throws SQLException
	{
	    Statement stmt = conn.createStatement();
	    ResultSet rs = stmt.executeQuery("select objectname, ATTRIBUTENAME, persistent from MAXATTRIBUTE order by objectname, ATTRIBUTENAME");
		 
	    while(rs.next())
	    {
	    	String a = rs.getString(1) + "." + rs.getString(2);
	    	//MxDdEntry entry = new MxDdEntry(a.toUpperCase(), rs.getString(3));
		    String aaa = rs.getString(3);
		    attrs.put(a, aaa);
	    }
	    
	    stmt = conn.createStatement();
	    ResultSet rs2 = stmt.executeQuery("select parent, name, child from MAXRELATIONSHIP order by parent, name");

	    while(rs2.next())
	    {
	    	String a = rs2.getString(1) + "." + rs2.getString(2);
	    	rels.put(a, rs2.getString(3));
	    }

	}
	
	public String getRelTable(String obj, String rel)
	{
		String entry = rels.get((obj + "." + rel).toUpperCase());
		if (entry==null)
		{
			//throw new RuntimeException("Relationship not found: " + obj + "." + rel);
			TidyApp.logInfo("Relationship not found " + obj + "." + rel);
			return rel;
		}
		return entry;
	}
	
	public String addRel(String obj, String id, String rel)
	{
		String table = getRelTable(obj, rel);
		rels.put((obj + "." + id).toUpperCase(), table);
		return table;
	}
	
	
	public boolean isPersistent(String obj, String attr)
	{
		String entry = attrs.get((obj + "." + attr).toUpperCase());
		if (entry==null)
		{
			//throw new RuntimeException("Attribute not found: " + obj + "." + attr);
			TidyApp.logInfo("Attribute not found " + obj + "." + attr);
			return false;
		}
		return entry.equals("1");
	}
	
}
