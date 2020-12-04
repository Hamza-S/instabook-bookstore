package model;

import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Map;

import org.json.JSONObject;

import authentication.Authenticator;
import bean.BookBean;
import bean.UserBean;
import dao.AddressDAO;
import dao.BooksDAO;
import dao.UsersDAO;

public class Books {
	private static Books instance;
	private BooksDAO bDAO;
	private UsersDAO uDAO;
	private Authenticator auth;
	private AddressDAO aDAO;
 
	private Books() {
	 
	}
	public static Books getInstance() throws ClassNotFoundException {
		if (instance == null) {
			instance = new Books();
			instance.bDAO = new BooksDAO();
			instance.auth = new Authenticator();
			instance.uDAO = new UsersDAO();
			instance.aDAO = new AddressDAO();
		}
		return instance;
		
	}
	public Map<String, BookBean> getLibrary() throws SQLException {
		return bDAO.getLibrary();
	}
	public Map<String, BookBean> searchLibrary(String title) throws SQLException {
		return bDAO.searchLibrary(title);
	}
	public Map<String, BookBean> getBooksByCategory(String category) throws SQLException {
		return bDAO.getBooksByCategory(category);
	}
	
	public boolean registerUser (String fname, String lname, String username, String email, String password) throws NoSuchAlgorithmException, SQLException {
		boolean registered = false;
		if (auth.userExists(username)) {
			//throw error because user already exists
		}
		else {
			auth.registerUser(fname, lname, username, email, password);
			registered = true;
		}
		return registered;
	}
	
	public int insertAddress(String username, String address, String province, String country, String zip) throws SQLException {
		return aDAO.insertAddress(username, address, country, province, zip);
		
	}
	
	public String getAddressAttribute(String username, String attribute) throws SQLException {
		return aDAO.getAddressAttribute(username, attribute);
		
	}
	
	public boolean login(String username, String password) throws NoSuchAlgorithmException, SQLException {
		boolean authenticated = false;
		if (!auth.userExists(username)) {
			System.out.println("the username doesnt exist");
			//throw error because USER does not exist
		}
		else {
			
			authenticated = auth.authenticate(username, password);
		}
		return authenticated;
	}
	
	public UserBean getUserBean(String username) throws SQLException {
		return uDAO.getUserBean(username);
		
	}
	
	public String export_json(String bid) throws SQLException {
		//get book by bid -> convert into json -> return json
		BookBean book=bDAO.getBookById(bid);
		JSONObject jsonObj = new JSONObject(book);
		String json = jsonObj.toString(4);
		return json;
		
	}

	public String generateBookCards(Map<String,BookBean> data) {
		String books = "";
		BookBean b = null;
		for (Map.Entry<String, BookBean> i : data.entrySet()) {
			b = i.getValue();
			books += "<div class=\"col-md-4\">";
			books += "<div class=\"card mb-4 shadow-sm\">";
			books += "<svg class=\"bd-placeholder-img card-img-top\" width=\"100%\" height=\"225\" xmlns=\"http://www.w3.org/2000/svg\"\r\n" + 
					"preserveAspectRatio=\"xMidYMid slice\" focusable=\"false\" role=\"img\">\r\n" + 
					"<image href=\"https://upload.wikimedia.org/wikipedia/en/c/c9/Harry_Potter_and_the_Goblet_of_Fire_Poster.jpg\"></image>\r\n" + 
					"</svg>";
			books += "<div class=\"card-body\">";
			books += "<h6 class=\"card-text\"> " + b.getTitle() + "</h6>";
			books += "<h7 class=\"card-text\"> " + b.getCategory() + "</h7>";
			books += "<br />";
			books += "<h7 class=\"card-text\"> $" + b.getPrice() + "</h7>";
			books += "<br />";
			books += "<div class=\"btn-group\">";
			books += "<form action=\"bookStore\" method=\"GET\">";
			books += "<input type=\"hidden\" name=\"bid\" value=\"" + b.getBid() + "\" />";
			books += "<button action='submit' class=\"btn btn-sm btn-outline-secondary\" name='moreInfo' value=\"true\">More Info</button>";
			books += "</form>";
			books += "</div>";
			books += "</div>";
			books += "</div>";
			books += "</div>";

		}
		return books;
		
	}
	
	public BookBean getBook(String bid) throws SQLException {
		BookBean book=bDAO.getBookById(bid);
		return book;
	}
}