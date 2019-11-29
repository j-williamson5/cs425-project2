package edu.jsu.mcis.cs425.project2;

import java.sql.SQLException;
import java.util.HashMap;

public class BeanApplicant {
    
    private String username;
    private int userid;
    private String displayname;
    private String[] skills;
    private String[] jobs;
    
    public void setJobsList() throws SQLException {
        
        //Make Datbase
        Database db = new Database();
        
        //Set jobs
        db.setJobsList(userid, jobs);
    }
    
    public void setUserInfo() throws SQLException{

        //Create an instance of the database class
        Database db = new Database();

        //Get the info from the database into a hashmap
        HashMap<String,String> userinfo = db.getUserInfo(username);

        //Update info
        userid = Integer.parseInt(userinfo.get("userid"));
        displayname = userinfo.get("displayname");
        
    }
    
    public void setSkillsList() throws SQLException{
        
        //Make Database
        Database db = new Database();
        
        //Set skills
        db.setSkillsList(userid,skills);
    }
    
    public String getJobsList() throws SQLException{
        
        //Make Database
        Database db = new Database();
        
        return (db.getJobsListAsHTML(userid, skills));
    }
    
    public String getSkillsList() throws SQLException{
        
        //Make Database
        Database db = new Database();
        
        return(db.getSkillsListAsHTML(userid));
    }
    
    public String[] getSkills(){
        return skills;
    }
    
    public void setSkills(String[] skills){
        this.skills = skills;
    }
    
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getUserid() {
        return userid;
    }

    public void setUserid(int userid) {
        this.userid = userid;
    }

    public String getDisplayname() {
        return displayname;
    }

    public void setDisplayname(String displayname) {
        this.displayname = displayname;
    }

    public String[] getJobs() {
        return jobs;
    }

    public void setJobs(String[] jobs) {
        this.jobs = jobs;
    }
    
    
}