package lab6plh302;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class DbApp {
	
	Connection conn;
	Scanner scn = new Scanner(System.in);

	public DbApp() {
		try {
			Class.forName("org.postgresql.Driver");
			System.out.println("-------- PostgreSQL JDBC Connection Testing ------------");
			System.out.println("PostgreSQL JDBC Driver Registered!");

			
			conn  = DriverManager.getConnection(
					"jdbc:postgresql://localhost:5432/final_project", "postgres", "0000");
			conn.setAutoCommit(false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (conn != null) {
			System.out.println("You made it, take control your database now!");
		} else {
			System.out.println("Failed to make connection!");}
	}
	
	// Diavazei tin epilogi tou xristi gia to menou
	public int waitForChoice() {
		int choice;
		
		System.out.println("Choice what you want to do:");
		System.out.println("1: Start Connection ");
		System.out.println("2: Show the grades of the student ");
		System.out.println("3: Change the grade of a lesson ");
		System.out.println("4: Back-up a table of the database ");
		System.out.println("5: Exit ");


		
		System.out.println("Make a choice ...");
		choice = scn.nextInt();
		
		return choice;
	}
	
	
	// tiponei thn analitiki vatmologia tou mathiti
	public void printGradesAm() {
		
		System.out.println("Give the Student AM:");
		int am= scn.nextInt();
		int nlesson=0;
		try {
			Statement st = conn.createStatement();
			ResultSet res =  st.executeQuery("SELECT course_code,final_grade FROM \"Register\" WHERE amka="+am + "AND register_status='pass'");
			System.out.println("The grades of the passed lesson are:");
			while (res.next())
				{
					System.out.println("Lesson:"+res.getString(1)+"    " +
						   "Grade:"+ res.getInt(2));
					nlesson++;
				
				}
			System.out.println("***********************************************");
			System.out.println("The student has passed  "+ nlesson + " lessons");
	
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	//alazei ton vathmo tou mathimatos enos sigekrimenoy xristi
	public void changeGrade() {
		
		System.out.println("Give the Student AM:");
		int am= scn.nextInt();
		System.out.println("Give the lesson code:");
		scn.nextLine();
		String code = scn.nextLine();
		System.out.println("Give the new lesson grade:");
		int grade = scn.nextInt();
	
		int affectedrows = 0; 
		String updateTableSQL = "UPDATE \"Register\" SET final_grade=? WHERE amka=? AND course_code=?  AND "
				+ "serial_number IN (SELECT serial_number FROM \"Register\" WHERE amka=? AND course_code=? ORDER BY serial_number DESC LIMIT 1)";
				
		try {
			PreparedStatement pStatement= conn.prepareStatement(updateTableSQL);
			
			pStatement.setInt(1,grade);
			pStatement.setInt(2,am);
			pStatement.setString(3,code);
			pStatement.setInt(4,am);
			pStatement.setString(5,code);

			
			
			affectedrows=pStatement.executeUpdate();
			pStatement.close();
			System.out.println(affectedrows); 
			
			
			
			
		}	catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		
		
	}
	
	// KANOUME BACKUP TO PINAKA 
	public void createBackUp()  {
		scn.nextLine(); //katharizw ton buffer
		System.out.println("Give the name of the table you want to back-up:");
		String name_table= scn.nextLine();
		System.out.println("Give the name of the back up database:");
		String name_backup= scn.nextLine();
		String table_data = null;
		String backup_table_data = null;
		
		// kanoume to connection me thn basei pou theloume na kanoume to backup
		Connection conn2 = null;
		try
		{
			conn2  = DriverManager.getConnection("jdbc:postgresql://localhost:5432/"+ name_backup , "postgres", "0000");
			//conn2.setAutoCommit(false);

		}
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();	
		}
		System.out.println("We have connected to the back-up database!!!"); //efoson perasei tin ekseresh tha exei kanei connection
		
		//pairnoume ta stoixeia tou pinaka apo tin vasiki mas vasi
		try {
		Statement sinEt = conn.createStatement();
		ResultSet st_Pin = sinEt.executeQuery("SELECT generateTableDDL('"+name_table+"')");
		while (st_Pin.next()) {
		    //System.out.println(st_Pin.getString(1));
		    table_data=st_Pin.getString(1); //to table_data exei mesa to create table
		   
		}		
		st_Pin.close();
		} catch (SQLException e) {
		// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (table_data!=null)	// an yparxei o pinakas ston originall database 8a treksei to if allios null
		{ 
			
			//pername to idia akrivos stoixeia apo ton back up 
			try 
			{
				Statement sinEt = conn2.createStatement();
				ResultSet st_Pin = sinEt.executeQuery("SELECT generateTableDDL('"+name_table+"')");
				while (st_Pin.next())
				{
					//System.out.println(st_Pin.getString(1));
					backup_table_data=st_Pin.getString(1);
				}		
				st_Pin.close();
			} catch (SQLException e) 
			{
				// TODO Auto-generated catch block
					e.printStackTrace();
			}
			
			if (table_data.equals(backup_table_data)) { 	//an kai sto backup kai ston original database iparxei o pinakas tote apla emeis
				
				try {
					PreparedStatement  sinEt = conn2.prepareStatement("TRUNCATE  \""+ name_table + "\"");	
					try {
					sinEt.executeUpdate();		//diagrafoume ta stoixeia tou pinaka
					}catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					sinEt.close();
					PreparedStatement  sinEt2 = conn.prepareStatement("SELECT * FROM \"" + name_table+"\"",ResultSet.TYPE_SCROLL_SENSITIVE, 
	                        ResultSet.CONCUR_UPDATABLE);	
					ResultSet st_Pin = sinEt2.executeQuery(); // pername stin resultset  ta stoixeia tou pinaka
				
					Object[] tData= new Object[st_Pin.getMetaData().getColumnCount()]; // dimiourgoume ena pinaka pou tha apothikeysoume ta dedomena tou pinaka 
					String inser;

					String updateTableSQL;
					PreparedStatement insBack = null;
					
					
					while (st_Pin.next())
					{
							inser="(";
							for (int i = 1; i < st_Pin.getMetaData().getColumnCount() + 1; i++) 
							{
								tData[i-1]=st_Pin.getObject(i);
								//System.out.println("  : "+ tData[i-1]);
								if (i==st_Pin.getMetaData().getColumnCount()) {
									inser= inser + "'" + tData[i-1] +"'"; 
								}
								else {
								inser= inser + "'" + tData[i-1] + "'" + ","; //grafoume gia to values
								}
								
				            }
							
							inser=inser +")";
							
							updateTableSQL="INSERT INTO \"" + name_table + "\" VALUES " + inser+ ";";
							//System.out.println(updateTableSQL);
							insBack= conn2.prepareStatement(updateTableSQL);
							int affectedrows=insBack.executeUpdate();
							insBack.close();
							//System.out.println(affectedrows); 
						
							
					}
					insBack.close();
					conn2.close();
					
					 
					
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				
			}else {	//an den iparxei o pinakas ton ftiaxnoume
				PreparedStatement sinEt;
				try 
				{
					sinEt = conn2.prepareStatement(table_data);
					sinEt.executeUpdate(); // dimiourgoume to pinaka!
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("We have created the table in back up database!!!!");
				try {
					PreparedStatement  sinEt2 = conn.prepareStatement("SELECT * FROM \"" + name_table+"\"",ResultSet.TYPE_SCROLL_SENSITIVE, 
	                        ResultSet.CONCUR_UPDATABLE);	
					ResultSet st_Pin = sinEt2.executeQuery(); // pername stin resultset  ta stoixeia tou pinaka
				
					Object[] tData= new Object[st_Pin.getMetaData().getColumnCount()]; // dimiourgoume ena pinaka pou tha apothikeysoume ta dedomena tou pinaka 
					String inser;

					String updateTableSQL;
					PreparedStatement insBack = null;
					
					
					while (st_Pin.next())
					{
							inser="(";
							for (int i = 1; i < st_Pin.getMetaData().getColumnCount() + 1; i++) 
							{
								tData[i-1]=st_Pin.getObject(i);
								//System.out.println("  : "+ tData[i-1]);
								if (i==st_Pin.getMetaData().getColumnCount()) {
									inser= inser + "'" + tData[i-1] +"'"; 
								}
								else {
								inser= inser + "'" + tData[i-1] + "'" + ","; //grafoume gia to values
								}
								
				            }
							
							inser=inser +")";
							
							updateTableSQL="INSERT INTO \"" + name_table + "\" VALUES " + inser+ ";";
							//System.out.println(updateTableSQL);
							insBack= conn2.prepareStatement(updateTableSQL);
							insBack.executeUpdate();
							insBack.close();
							//System.out.println(affectedrows); 
						
							
					}
					insBack.close();
					conn2.close();
					
					System.out.println("We have inserted the data in the database!!!!"); 
					System.out.println("We have created the table in back up database!!!!");

				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				
				
				
			}
			
			
			
		}else { //den iparxei pinakas me auto to onoma
			System.out.println("The table doesnt exist in the original database!!");
		}
	
		
	}
	
	
	
	
	public void commit() {
		try {
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void abort() {
		try {
			conn.rollback();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
		
	

	
	public static void main(String[] args) {
		DbApp dbapp = new DbApp();
		int choice =0; 
		
		do 
		{
		choice= dbapp.waitForChoice();
		switch (choice) {
			case 1:
				System.out.println("The connection is made to the database!!!");
                break;
			case 2:
				dbapp.printGradesAm();
                break;
			case 3:
				dbapp.changeGrade();
				break;
			case 4:
				dbapp.createBackUp();
				break;
			case 5:
				System.out.println("Goodbye!!!");
				break;
				
		}
			
		}
		while (choice!=5);
		
		dbapp.abort();
		
	
		
	}

}
