package kz.smpp.mysql;


import java.sql.Statement;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;


public class MyDBConnection {

    private static Connection myConnection;
    private Statement statement;

    public MyDBConnection() {
        init();
    }

    public void init(){

        try{

            Class.forName("com.mysql.jdbc.Driver");
            myConnection=DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1/smpp_clients?characterEncoding=utf8","root", ""
            );
        }
        catch(Exception e){
            System.out.println("Failed to get connection");
            e.printStackTrace();
        }
    }


    public Connection getMyConnection(){
        return myConnection;
    }


    public void close(ResultSet rs){

        if(rs !=null){
            try{
                rs.close();
            }
            catch(Exception e){}

        }
    }

    public void close(java.sql.Statement stmt){

        if(stmt !=null){
            try{
                stmt.close();
            }
            catch(Exception e){}

        }
    }

    public void destroy(){

        if(myConnection !=null){

            try{
                myConnection.close();
            }
            catch(Exception e){}


        }
    }

    /**
     *
     * @param query String The query to be executed
     * @return a ResultSet object containing the results or null if not available
     * @throws SQLException
     */
    public ResultSet query(String query) throws SQLException{
        statement = myConnection.createStatement();
        ResultSet res = statement.executeQuery(query);
        return res;
    }
    /**
     * @desc Method to insert data to a table
     * @param insertQuery String The Insert query
     * @return boolean
     * @throws SQLException
     */
    public int Update(String insertQuery) throws SQLException {
        statement = myConnection.createStatement();
        int result = statement.executeUpdate(insertQuery);
        return result;
    }

    public int getLastId() throws SQLException {
        int result = -1;
        String lastIdString = "SELECT LAST_INSERT_ID() as LIID";
        statement = myConnection.createStatement();
        ResultSet res = statement.executeQuery(lastIdString);
        if (res.next()){
            result = res.getInt("LIID");
        }
        return result;
    }

    public client getClient(int id){
        client l_client = new client();
        String sql_string = "SELECT * FROM clients WHERE id = "+ id;
        try {
            ResultSet rs = this.query(sql_string);
            if (rs.next()) {
                l_client.setId(rs.getInt("id"));
                l_client.setAddrs(rs.getLong("msisdn"));
                l_client.setStatus(rs.getInt("status"));
            }
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }
        return l_client;
    }

    public client getClient(long msisdn){
        client l_client = new client();
        String sql_string = "SELECT * FROM clients WHERE msisdn = "+ msisdn;
        try {
            ResultSet rs = this.query(sql_string);
            if (rs.next()) {
                l_client.setId(rs.getInt("id"));
                l_client.setAddrs(rs.getLong("msisdn"));
                l_client.setStatus(rs.getInt("status"));
            }
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }
        return l_client;
    }

    public client setNewClient(long msisdn){
        client l_client = new client();
        String sql_string = "SELECT * FROM clients WHERE msisdn= "+msisdn;
        try {
            ResultSet rs = this.query(sql_string);
            if (rs.next()) {
                if (rs.getInt("status") != 0){
                    sql_string = "UPDATE clients SET  status = 0 WHERE msisdn ="+ msisdn;
                    this.Update(sql_string);
                }
                else {
                    l_client.setStatus(0);
                    l_client.setAddrs(msisdn);
                    l_client.setId(rs.getInt("id"));
                }
            }
            else{
                sql_string = "INSERT INTO clients VALUES(null,"+ msisdn+",0)";
                this.Update(sql_string);
                l_client.setId(this.getLastId());
                l_client.setAddrs(msisdn);
                l_client.setStatus(0);
            }
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }
        return l_client;
    }

    public LinkedList<ContentType> getClientsContentTypes(client l_client) {
        LinkedList <ContentType> lct = new LinkedList<>();
        String sql_string = "SELECT content_type.* FROM client_content_type left join content_type " +
                "on content_type.id = client_content_type.id_content_type WHERE client_content_type.status = 0 and client_content_type.id_client = "+ l_client.getId();
        try {
            ResultSet rs = this.query(sql_string);
            while (rs.next()) {
                ContentType ct = new ContentType();
                ct.setId(rs.getInt("id"));
                ct.setName(rs.getString("name"));
                ct.setName_eng(rs.getString("name_eng"));
                ct.setTable_name(rs.getString("table_name"));
                lct.add(ct);
            }
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }
        return lct;
    }

    public client setStopClient(long msisdn){
        client l_client = new client();
        String sql_string = "SELECT * FROM clients WHERE msisdn= "+msisdn;
        try {
            ResultSet rs = this.query(sql_string);
            if (rs.next()) {
                if (rs.getInt("status") != 0){
                    sql_string = "UPDATE clients SET  status = -1 WHERE msisdn ="+ msisdn;
                    this.Update(sql_string);
                }
                else {
                    l_client.setStatus(-1);
                    l_client.setAddrs(msisdn);
                    l_client.setId(rs.getInt("id"));
                }
            }
            else{
                sql_string = "INSERT INTO clients VALUES(null,"+ msisdn+",-1)";
                this.Update(sql_string);
                l_client.setId(this.getLastId());
                l_client.setAddrs(msisdn);
                l_client.setStatus(-1);
            }
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }
        return l_client;
    }

    public LinkedList<ContentType> getClientsContentTypes(client l_client, ContentType contenttype) {
        LinkedList <ContentType> lct = new LinkedList<>();
        String sql_string = "SELECT content_type.* FROM client_content_type left join content_type " +
                "on content_type.id = client_content_type.id_content_type WHERE client_content_type.id_client = "+ l_client.getId() +" " +
                " AND client_content_type.id_content_type = "+contenttype.getId();
        try {
            ResultSet rs = this.query(sql_string);
            while (rs.next()) {
                ContentType ct = new ContentType();
                ct.setId(rs.getInt("id"));
                ct.setName(rs.getString("name"));
                ct.setName_eng(rs.getString("name_eng"));
                ct.setTable_name(rs.getString("table_name"));
                lct.add(ct);
            }
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }
        return lct;
    }

    public boolean setNewClientsContentTypes(client l_client, ContentType contentType) {
        boolean result= false;

        String sql_string = "INSERT INTO client_content_type(id_client, id_content_type, status) " +
                "VALUES ("+ l_client.getId() + ","+contentType.getId() +",0)";
        try {
            this.Update(sql_string);
            result = true;
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }
        return result;
    }

    public ContentType getContentType(int idtype){
        ContentType ct = new ContentType();
        String sql_string = "SELECT * FROM content_type WHERE id = "+ idtype;
        try {
            ResultSet rs = this.query(sql_string);
            if (rs.next()) {
                ct.setId(rs.getInt("id"));
                ct.setName(rs.getString("name"));
                ct.setName_eng(rs.getString("name_eng"));
                ct.setTable_name(rs.getString("table_name"));
            }
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }
        return ct;
    }
    public ContentType getContentType(String table_name){
        ContentType ct = new ContentType();
        String sql_string = "SELECT * FROM content_type WHERE table_name = '"+ table_name+"'";
        try {
            ResultSet rs = this.query(sql_string);
            if (rs.next()) {
                ct.setId(rs.getInt("id"));
                ct.setName(rs.getString("name"));
                ct.setName_eng(rs.getString("name_eng"));
                ct.setTable_name(rs.getString("table_name"));
            }
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }
        return ct;
    }

}
