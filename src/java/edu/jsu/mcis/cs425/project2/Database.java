package edu.jsu.mcis.cs425.project2;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;


public class Database {
    
    private Connection conn;
    
    public Database(){
        this.conn = getConnection();
    }
    
     public Connection getConnection() {
        
        conn = null;
        
        try {
            Context envContext = new InitialContext();
            Context initContext  = (Context)envContext.lookup("java:/comp/env");
            DataSource ds = (DataSource)initContext.lookup("jdbc/db_pool");
            System.err.println("*** B4 Connection");
            conn = ds.getConnection();
            System.out.println("*** CONNECTION SUCCESSFUL");
        }        
        catch (Exception e) {
            e.printStackTrace();
        }
        if(conn == null){
            System.out.println("NULL CONNECTION");
        }
        return conn;

    }
     
     public HashMap getUserInfo(String username) throws SQLException{
         
         //Get result set with Query
         String query = "SELECT id,displayname FROM cs425_p2.user WHERE cs425_p2.user.username = \'" + username + "\'";
         ResultSet results = getResultSet(query);
         
         //Extract info from resultset
         results.next();
         Integer userid = results.getInt("id");
         String displayName = results.getString("displayname");
         
         //Put info into map
         HashMap<String,String> map = new HashMap<>();
         map.put("userid",userid.toString());
         map.put("displayname",displayName);
         
         return map;
     }
     
     private ResultSet getResultSet(String query) throws SQLException{
        
        //Get connection and make prepared statment
        //Connection conn = getConnection();
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.execute();
        
        ResultSet results = pstmt.getResultSet();
        
        //Execute Query and Return ResultSet
        return results;
    }
     
     public String getSkillsListAsHTML(int userid) throws SQLException{
         
         //Get a list of all skills
         String query = "SELECT * FROM cs425_p2.skills";
         ResultSet rs = getResultSet(query);
         HashMap<Integer,String> skills = new HashMap<>();
         
         //Get info out of the result set and map it
         while(rs.next()){
             
             //Grab info
             Integer id = rs.getInt("id");
             String description = rs.getString("description");
             
             //Put info into map
             skills.put(id,description);
         }
         
         //Get a list of all user skills
         String userQuery = "SELECT skillsid FROM cs425_p2.applicants_to_skills WHERE cs425_p2.applicants_to_skills.userid = \'" + String.valueOf(userid) + "\'";
         ResultSet userRS = getResultSet(userQuery);
         
         //"Map" user skills
         ArrayList<Integer> userSkills = new ArrayList<>();
         
         while(userRS.next()){
             userSkills.add(userRS.getInt("skillsid"));
         }
         
         //Make skills string for exporting
         String output = "";
         
         
         //Loop through the skills list 
         for (Integer skillid : skills.keySet()){
             String skillsid = String.valueOf(skillid);
             Boolean skillMatch = false;
             
             //Loop through the user skills list
             for(Integer userSkillid : userSkills){
                 
                 //This checks to see if the user has already selected a skill. That way, we can make the checkbox already checked.
                 if(Objects.equals(skillid, userSkillid)){
                     
                     //Set the skill match flag
                     skillMatch = true;
                     
                     //Append checkbox to output
                     output += "<input type=\'checkbox\' name=\'skills\' value=" + skillsid + " id=\'skills_" + skillsid + "\' checked> <label for=\'skills_" + skillsid+"\'>" + skills.get(skillid) + "</label><br />";
                 }
             }
             
             //If the skill wasn't matched to one the user had, add it without a checkbox
             if(!skillMatch){
                 
                 //Reset the skill match flag
                 skillMatch = false;
                 
                 //Append checkbox to output
                 output += "<input type=\'checkbox\' name=\'skills\' value=" + skillsid + " id=\'skills_" + skillsid + "> <label for=\'skills_" + skillsid+"\'>" + skills.get(skillid) + "</label><br />";
             }
         }
         
         return output;
     }
     
     public void setSkillsList(int userid, String[] skills) throws SQLException{
         
         System.err.println( Arrays.toString(skills) );
         //Get connection
         //Connection conn = getConnection();
         //THIS IS IMPORTANT FOR BATCHING
         conn.setAutoCommit(false);
         
         //DELETE previous records
         //Make a statment for the query
         PreparedStatement stmt = conn.prepareStatement("DELETE FROM cs425_p2.applicants_to_skills WHERE cs425_p2.applicants_to_skills.userid = \'" + userid + "\'");
         
         //execute the statment
         stmt.execute();
         
         //Redo statement for insert
         stmt = conn.prepareStatement("INSERT INTO cs425_p2.applicants_to_skills (userid,skillsid) VALUES (?, ?)");
         
         //Loop through skills and add them
         for(String skill : skills){
             stmt.setInt(1, userid);
             stmt.setString(2, skill);
             stmt.addBatch();
         }
         
         //Execute Batch
         stmt.executeBatch();
         conn.commit();
         stmt.close();
         conn.setAutoCommit(true);
     }
     
     public String getJobsListAsHTML(int userid, String[] skills) throws SQLException{
         
         //Set up output String
         String output = "";
         
         //Set up duplicate jobs flag
         Boolean inList = false;
         
         //Get list of all jobs
         String jobsQuery = "SELECT id,name FROM cs425_p2.jobs";
         ResultSet jobsRS = getResultSet(jobsQuery);
         HashMap<Integer,String> jobs = new HashMap<>();
         
         //Extract info out of the result set and map it
         while(jobsRS.next()){
             jobs.put(jobsRS.getInt("id"), jobsRS.getString("name"));
         }
         
         
         //Get list of all user's jobs
         String userJobQuery = "SELECT jobsid FROM cs425_p2.applicants_to_jobs WHERE cs425_p2.applicants_to_jobs.userid = \'" + String.valueOf(userid) + "\'";
         ResultSet userRS = getResultSet(userJobQuery);
         ArrayList<Integer> userJobs = new ArrayList<>();
         
         //Extract info from result set and append it
         while(userRS.next()){
             userJobs.add(userRS.getInt("jobsid"));
         }
         
         
         //Get a list of all skill job pairings
         String pairQuery = "SELECT skillsid,jobsid FROM cs425_p2.skills_to_jobs";
         ResultSet pairRS = getResultSet(pairQuery);
         HashMap<Integer,Integer> pairs = new HashMap<>();
         
         //Extract info from pairings resultset and map it
         while(pairRS.next()){
             pairs.put(pairRS.getInt("skillsid"),pairRS.getInt("jobsid"));
         }
         
         
         //Find reccomended jobs based on skills required for the job
         ArrayList<Integer> recJobs = new ArrayList<>();
         
         if(skills != null){
             
            for(Integer skillid : pairs.keySet()){
                
                //Loop through master pair list
                for(String skill : skills){
                    
                    //Check if the job is in the user's skillset
                    if(Objects.equals(Integer.parseInt(skill), skillid)){
                        
                        //Loop through jobs already reccomneded (to prevent doubles)
                        for(Integer job: recJobs){
                            if(Objects.equals(job, pairs.get(skillid))){
                                inList = true;
                            }
                        }
                        
                        //Add the job if it is not already recommended
                        if(!inList){
                            recJobs.add(pairs.get(skillid));
                        }
                        //if it was in the list, reset the duplicate flag
                        else{
                            inList = false;
                        }
                        
                    }
                }
            }
         }
         
         //Loop through and add jobs that are already selected
         for(Integer userJobid : userJobs){
             output += "<input type=\'checkbox\' name=\'jobs\' value=" + userJobid + " id=\'jobs_" + userJobid + "\' checked> <label for=\'jobs_" + userJobid+"\'>" + jobs.get(userJobid) + "</label><br />";
         }
         
         
         //Add jobs from reccomended to output
         inList = false;
         //Loop through all reccomended jobs
         for(Integer recJobid : recJobs){
             
             //Loop through all user chosen jobs
             for(Integer userJobid : userJobs){
                 
                 //Check if the job is already chosen by the user
                 if(Objects.equals(recJobid, userJobid)){
                     inList = true;
                 }
             }

            //If the job is not already selected by the user
             if(!inList){
                 output += "<input type=\'checkbox\' name=\'jobs\' value=" + recJobid + " id=\'jobs_" + recJobid + "\'> <label for=\'jobs_" + recJobid+"\'>" + jobs.get(recJobid) + "</label><br />";
             }
             else{
                 inList = false;
             }

             
         }
         
         
         return output;
     }
     
     public void setJobsList(int userid, String[] jobs) throws SQLException{
         
         System.err.println( Arrays.toString(jobs) );
                  
         //Get connection
         //Connection conn = getConnection();
         //THIS IS IMPORTANT FOR BATCHING
         conn.setAutoCommit(false);
         
         //DELETE previous records
         //Make a statment for the query
         PreparedStatement stmt = conn.prepareStatement("DELETE FROM cs425_p2.applicants_to_jobs WHERE cs425_p2.applicants_to_jobs.userid = \'" + userid + "\'");
         //execute the statment
         stmt.execute();
         System.err.println("Past Stmt" );
         //Redo statement for insert
         stmt = conn.prepareStatement("INSERT INTO cs425_p2.applicants_to_jobs (userid,jobsid) VALUES (?, ?)");
         
         //Loop through jobs and add them
         for(String job : jobs){
             stmt.setInt(1, userid);
             stmt.setString(2, job);
             stmt.addBatch();
         }
         
         //Execute Batch
         stmt.executeBatch();
         conn.commit();
         stmt.close();
         conn.setAutoCommit(true);
         System.err.println("End of Jobs List" );
     }
}
