package edu.wpi.teamname.database;

import edu.wpi.teamname.database.interfaces.ServiceRequestDAO;
import edu.wpi.teamname.servicerequest.ServiceRequest;
import edu.wpi.teamname.servicerequest.Status;
import edu.wpi.teamname.servicerequest.requestitem.RequestItem;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ServiceRequestDAOImpl implements ServiceRequestDAO {
  /** */
  @Override
  public void sync(ServiceRequest serviceRequest) throws SQLException {
    Connection connection = DataManager.DbConnection();
    try (connection) {
      String query =
          "UPDATE \"ServiceRequest\" SET \"roomNum\" = ?, \"staffName\" = ?, \"patientName\" = ?, \"requestedAt\" = ?, \"deliverBy\" = ?, \"status\" = ?, \"requestMadeBy\" = ?, \"requestID\" = ?"
              + " WHERE \"requestID\" = ?";
      PreparedStatement statement = connection.prepareStatement(query);
      statement.setString(1, serviceRequest.getRoomNumber());
      statement.setString(2, serviceRequest.getPatientName());
      statement.setTimestamp(3, serviceRequest.getRequestedAt());
      statement.setTimestamp(4, serviceRequest.getDeliverBy());
      statement.setString(5, serviceRequest.getStatus().getStatusString());
      statement.setString(6, serviceRequest.getRequestMadeBy());
      statement.setInt(7, serviceRequest.getRequestID());
      statement.setInt(8, serviceRequest.getOriginalID());
      statement.executeUpdate();
    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }
    connection.close();
  }

  /** @return */
  @Override
  public ArrayList<ServiceRequest> getAll() throws SQLException {
    Connection connection = DataManager.DbConnection();
    ArrayList<ServiceRequest> list = new ArrayList<ServiceRequest>();

    try (connection) {
      String query = "SELECT * FROM \"ServiceRequest\"";
      PreparedStatement statement = connection.prepareStatement(query);
      ResultSet rs = statement.executeQuery();

      while (rs.next()) {
        int requestID = rs.getInt("requestID");
        String roomNum = rs.getString("roomNum");
        String staffName = rs.getString("staffName");
        String patientName = rs.getString("patientName");
        Timestamp requestedAt = rs.getTimestamp("requestedAt");
        Timestamp deliverBy = rs.getTimestamp("deliverBy");
        Status status = Status.valueOf(rs.getString("status"));
        String requestMadeBy = rs.getString("requestMadeBy");
        list.add(
            new ServiceRequest(
                requestID,
                staffName,
                patientName,
                roomNum,
                deliverBy,
                requestedAt,
                status,
                requestMadeBy));
      }
    }
    connection.close();
    return list;
  }

  /** @param serviceRequest */
  @Override
  public void add(ServiceRequest serviceRequest) throws SQLException {
    Connection connection = DataManager.DbConnection();
    try {
      String query =
          "INSERT INTO \"ServiceRequest\" (\"requestID\", \"roomNum\", \"staffName\", \"patientName\", \"requestedAt\", \"deliverBy\", \"status\", \"requestMadeBy\") VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
      PreparedStatement statement = connection.prepareStatement(query);
      statement.setInt(1, serviceRequest.getRequestID());
      statement.setString(2, serviceRequest.getRoomNumber());
      statement.setString(3, serviceRequest.getStaffName());
      statement.setString(4, serviceRequest.getPatientName());
      statement.setTimestamp(5, serviceRequest.getRequestedAt());
      statement.setTimestamp(6, serviceRequest.getDeliverBy());
      statement.setString(7, serviceRequest.getStatus().getStatusString());
      statement.setString(8, serviceRequest.getRequestMadeBy());

      statement.executeUpdate();
      // ItemsOrdered
      ArrayList<RequestItem> items = serviceRequest.getItems();
      for (int i = 0; i < items.size(); i++) {
        connection = DataManager.DbConnection();
        int newQuantity = getQuantity(serviceRequest.getRequestID(), items.get(i).getItemID()) + 1;
        try {

          if (newQuantity == 1) {
            query =
                "INSERT INTO \"ItemsOrdered\" (\"requestID\", \"itemID\", \"quantity\") "
                    + "VALUES (?, ?, 1)";
            statement = connection.prepareStatement(query);
            statement.setInt(1, serviceRequest.getRequestID());
            statement.setInt(2, items.get(i).getItemID());
          } else {
            query =
                "UPDATE \"ItemsOrdered\" SET quantity = ? WHERE \"itemID\" = ? AND \"requestID\" = ?";
            statement = connection.prepareStatement(query);
            statement.setInt(1, newQuantity);
            statement.setInt(2, items.get(i).getItemID());
            statement.setInt(3, serviceRequest.getRequestID());
          }
          statement = connection.prepareStatement(query);
          statement.executeUpdate();
        } catch (SQLException e) {
          System.out.println(e.getMessage());
        }
      }

    } catch (SQLException e) {
      System.err.println(e.getMessage());
    }
    connection.close();
  }

  public int getQuantity(int requestID, int itemID) throws SQLException {
    Connection connection = DataManager.DbConnection();

    int quantity = 0;
    try (connection) {
      String query =
          "SELECT \"quantity\" FROM \"ItemsOrdered\" WHERE \"itemID\" = ? AND \"requestID\" = ?";
      PreparedStatement statement = connection.prepareStatement(query);
      ResultSet rs = statement.executeQuery();

      while (rs.next()) {
        quantity = rs.getInt("quantity");
      }
    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }
    if (quantity > 0) {
      return quantity + 1;
    } else {
      return 1;
    }
  }

  /** @param serviceRequest */
  @Override
  public void delete(ServiceRequest serviceRequest) throws SQLException {
    Connection connection = DataManager.DbConnection();
    String query = "DELETE FROM \"ServiceRequest\" WHERE \"requestID\" = ?";
    try (connection) {
      PreparedStatement statement = connection.prepareStatement(query);
      statement.setInt(1, serviceRequest.getRequestID());
      statement.executeUpdate();
    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }
    try (Statement statement = connection.createStatement()) {
      ResultSet rs2 = statement.executeQuery(query);
      int count = 0;
      while (rs2.next()) count++;
      if (count == 0) System.out.println("ServiceRequest information deleted successfully.");
      else System.out.println("ServiceRequest information did not delete.");
    } catch (SQLException e2) {
      System.out.println("Error checking delete. " + e2);
    }
    connection.close();
  }

  public void deleteWithItems(ServiceRequest serviceRequest) throws SQLException {
    Connection connection = DataManager.DbConnection();
    String query = "DELETE FROM \"ServiceRequest\" WHERE \"requestID\" = ?";
    try (connection) {
      PreparedStatement statement = connection.prepareStatement(query);
      statement.setInt(1, serviceRequest.getRequestID());
      statement.executeUpdate();
      query = "DELETE FROM \"ItemsOrdered\" WHERE \"requestID\" = ?";
      statement = connection.prepareStatement(query);
      statement.setInt(1, serviceRequest.getRequestID());
      statement.executeUpdate();

    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }
    try (Statement statement = connection.createStatement()) {
      ResultSet rs2 = statement.executeQuery(query);
      int count = 0;
      while (rs2.next()) count++;
      if (count == 0) System.out.println("ServiceRequest information deleted successfully.");
      else System.out.println("ServiceRequest information did not delete.");
    } catch (SQLException e2) {
      System.out.println("Error checking delete. " + e2);
    }
    connection.close();
  }

  public static ServiceRequest getServiceRequest(int id) throws SQLException {
    Connection connection = DataManager.DbConnection();
    String query = "SELECT * FROM \"ServiceRequest\" WHERE \"requestID\" = ?";
    ServiceRequest serviceRequest = null;
    try (connection) {
      PreparedStatement statement = connection.prepareStatement(query);
      statement.setInt(1, id);
      ResultSet rs = statement.executeQuery();

      int rID = rs.getInt("mealID");
      String roomNum = rs.getString("roomNum");
      String staffName = rs.getString("Meal");
      String patientName = rs.getString("Cuisine");
      Timestamp requestedAt = rs.getTimestamp("requestedAt");
      Timestamp deliverBy = rs.getTimestamp("deliverBy");
      Status status = Status.valueOf(rs.getString("status"));
      String requestMadeBy = rs.getString("requestMadeBy");
      serviceRequest =
          (new ServiceRequest(
              rID, staffName, patientName, roomNum, deliverBy, requestedAt, status, requestMadeBy));
    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }
    return serviceRequest;
  }

  /**
   * * given an id and a staffname, updates that request's staff name into the new staff name
   *
   * @param requestID the id of the request to update
   * @param staffName the new staff name
   */
  public static void uploadStaffName(int requestID, String staffName) throws SQLException {
    Connection connection = DataManager.DbConnection();
    try {
      String query = "UPDATE \"ServiceRequest\" SET \"staffName\" = ? WHERE \"requestID\" = ?";
      PreparedStatement statement = connection.prepareStatement(query);
      statement.setString(1, staffName);
      statement.setInt(2, requestID);
      statement.executeUpdate();
      connection.close();
    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }
  }
  /**
   * Exports data from a PostgreSQL database table "ServiceRequest" to a CSV file
   *
   * @param csvFilePath a String representing the csv data (must use "//" not "/")
   * @throws SQLException if an error occurs while exporting the data from the database
   * @throws IOException if an error occurs while writing the data to the file
   */
  public static void exportServiceRequestToCSV(String csvFilePath)
      throws SQLException, IOException {
    ServiceRequestDAOImpl serviceRequestDAO = new ServiceRequestDAOImpl();
    ArrayList<ServiceRequest> serviceRequests = serviceRequestDAO.getAll();

    FileWriter writer = new FileWriter(csvFilePath);
    writer.write(
        "Request ID,Staff Name,Patient Name,Room Number,Requested At,Deliver By,Status,Request Made By\n");

    for (ServiceRequest sr : serviceRequests) {
      writer.write(
          sr.getRequestID()
              + ","
              + sr.getStaffName()
              + ","
              + sr.getPatientName()
              + ","
              + sr.getRoomNumber()
              + ","
              + sr.getRequestedAt()
              + ","
              + sr.getDeliverBy()
              + ","
              + sr.getStatus().getStatusString()
              + ","
              + sr.getRequestMadeBy()
              + "\n");
    }
    writer.close();
  }
  /**
   * Uploads CSV data to a PostgreSQL database table "ServiceRequest"
   *
   * @param csvFilePath is a String representing the filepath of the file we want to upload (use
   *     "\\" instead of "\")
   * @throws SQLException if an error occurs while uploading the data to the database
   */
  public static void uploadServiceRequestToPostgreSQL(String csvFilePath) throws SQLException {
    List<String[]> csvData;
    Connection connection = DataManager.DbConnection();
    DataManager dataImport = new DataManager();
    csvData = dataImport.parseCSVAndUploadToPostgreSQL(csvFilePath);

    try (connection) {
      DatabaseMetaData dbm = connection.getMetaData();
      ResultSet tables = dbm.getTables(null, null, "Node", null);
      if (!tables.next()) {
        PreparedStatement createTableStatement =
            connection.prepareStatement(
                "CREATE TABLE \"ServiceRequest\" (\"requestID\" INTEGER NOT NULL, \"roomNum\" VARCHAR(255) NOT NULL, \"staffName\" VARCHAR(255) NOT NULL, \"patientName\" VARCHAR(255) NOT NULL, \"requestedAt\" TIMESTAMP NOT NULL, \"deliverBy\" TIMESTAMP NOT NULL, \"status\" VARCHAR(255) NOT NULL, \"requestMadeBy\" VARCHAR(255), PRIMARY KEY (\"requestID\"))");
        createTableStatement.executeUpdate();
      }

      String query =
          "INSERT INTO \"ServiceRequest\" (\"requestID\", \"roomNum\", \"staffName\", \"patientName\", \"requestedAt\", \"deliverBy\", \"status\", \"requestMadeBy\") "
              + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
      PreparedStatement statement =
          connection.prepareStatement("TRUNCATE TABLE \"ServiceRequest\";");
      statement.executeUpdate();
      statement = connection.prepareStatement(query);

      for (int i = 1; i < csvData.size(); i++) {
        String[] row = csvData.get(i);

        // Check if row has enough columns
        if (row.length < 8) {
          System.err.println("Skipping row " + i + " due to missing columns");
          continue;
        }

        statement.setInt(1, Integer.parseInt(row[0]));
        statement.setString(2, row[3]); // Swap roomNum and patientName columns
        statement.setString(3, row[1]);
        statement.setString(4, row[2]);

        // Parse date columns only if they are not "BLANK"
        if (!"BLANK".equals(row[4])) {
          SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
          java.util.Date parsedDate = dateFormat.parse(row[4]);
          java.sql.Date date = new java.sql.Date(parsedDate.getTime());
          statement.setDate(5, date);
        } else {
          statement.setNull(5, Types.DATE);
        }

        if (!"BLANK".equals(row[5])) {
          SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
          java.util.Date parsedDate = dateFormat.parse(row[5]);
          java.sql.Date date = new java.sql.Date(parsedDate.getTime());
          statement.setDate(6, date);
        } else {
          statement.setNull(6, Types.DATE);
        }

        // Assign default value "N/A" to status column if it is missing
        if (row[6] == null || row[6].isEmpty()) {
          statement.setString(7, "N/A");
        } else {
          statement.setString(7, row[6]);
        }

        statement.setString(8, row[7]);

        statement.executeUpdate();
      }
      System.out.println("CSV data uploaded to PostgreSQL database");
    } catch (SQLException e) {
      System.err.println("Error uploading CSV data to PostgreSQL database: " + e.getMessage());
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }
}
