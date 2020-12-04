package controller;

import model.Books;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import bean.BookBean;
import bean.ReviewBean;
import bean.UserBean;
import dao.BooksDAO;

@WebServlet({ "/bookStore", "/bookStore/*" })
public class bookStore extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public bookStore() {
		super();
	}

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		ServletContext svcnxt = getServletContext();
		try {
			svcnxt.setAttribute("model", Books.getInstance()); // Singleton design pattern for model
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Books book = (Books) request.getServletContext().getAttribute("model");
		HttpSession session = request.getSession();
		if (session.getAttribute("UserBean") == null) { // Initialize a guest user with an empty cart upon initial
														// visit/not logged in

			UserBean user = new UserBean();
			session.setAttribute("UserBean", user);
		} else {
			UserBean user = (UserBean) session.getAttribute("UserBean");
			;
			System.out.println(Arrays.toString(user.getCart().getCart().entrySet().toArray()));
			// Do nothing, userbean already generated, or the user is logged in.
		}

		String path = request.getPathInfo();
		try {

			Map<String, BookBean> rv = book.getLibrary();
			String books = book.generateBookCards(rv);
			request.setAttribute("library", books);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (request.getParameter("search") != null && request.getParameter("search").equals("true")) { // Search
			String title = request.getParameter("bookTitle");
			try {

				Map<String, BookBean> rv = book.searchLibrary(title);
				String searchRes = book.generateBookCards(rv);
				request.setAttribute("searchResults", searchRes);
				request.setAttribute("resultCount", rv.size());
			} catch (Exception e) {
				e.printStackTrace();
			}
			request.getRequestDispatcher("/searchresults.jspx").forward(request, response);
		}
		// Request to a BookInfo more info
		else if (request.getParameter("moreInfo") != null && request.getParameter("moreInfo").equals("true")) {
			System.out.println("In more info");
			String bid = request.getParameter("bid");
			try {
				request.setAttribute("bid", book.getBook(bid).getBid());
				request.setAttribute("category", book.getBook(bid).getCategory());
				request.setAttribute("price", book.getBook(bid).getPrice());
				request.setAttribute("title", book.getBook(bid).getTitle());
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			request.getRequestDispatcher("/bookinfo.jspx").forward(request, response);

		} else if (request.getParameter("addtoCart") != null && request.getParameter("addtoCart").equals("true")) {
			System.out.println("asipdniasnd");
			UserBean s = (UserBean) request.getSession().getAttribute("UserBean");
			String bid = request.getParameter("bookid");
			int quantity = Integer.parseInt(request.getParameter("quantity"));
			System.out.println(bid);
			System.out.println(quantity);
			s.getCart().addItem(bid, quantity);
			System.out.println(Arrays.toString(s.getCart().getCart().entrySet().toArray()));
			request.getSession().setAttribute("UserBean", s);
			request.getSession().setAttribute("CartNum", s.getCart().getTotalQuantity());
			request.getRequestDispatcher("/bookStore?bid=" + bid + "&moreInfo=true").forward(request, response);

		}
		//Add a review if user is logged in
		else if (request.getParameter("reviewAdded") != null && request.getParameter("reviewAdded").equals("true")) { 
			System.out.println("in review added");
			boolean loggedIn=false;
			
			if(request.getSession().getAttribute("isLoggedIn")!=null) {
				loggedIn=(boolean)request.getSession().getAttribute("isLoggedIn");
			}
			
			
			if(loggedIn) {
				if(true) {//did user review the book already?
					String reviewTitle = request.getParameter("reviewTitle");
					String bid = request.getParameter("bid");
					System.out.println("reviewTitle:"+reviewTitle);
					String newReview = request.getParameter("newReview");
					System.out.println("newReview:"+newReview);
					String rating = request.getParameter("rating");
					System.out.println("rating:"+rating);
					
					try {
						int success=Books.getInstance().addReview("usama02", bid, reviewTitle, newReview, rating);
						System.out.println("sucessModel:"+success);
					} catch (ClassNotFoundException | SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else {
					System.out.println("Error: User already left a Review");
				}
			}
			else {
				System.out.println("Error: Guest tried to add a Review");
			}
			
			try {
				boolean reviewed=Books.getInstance().userReviewedTheBook("usama01", "b001");
				System.out.println("book reviewd:"+reviewed);
				ArrayList<ReviewBean> reviews=Books.getInstance().getBookReviews("b001");
				System.out.println("reviews:"+reviews.toString());
			} catch (ClassNotFoundException | SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}




			request.getRequestDispatcher("home.jspx").forward(request, response);

		}

		else if (request.getParameter("login") != null && request.getParameter("login").equals("true")) { // Login
																											// button
																											// handler
			String username = request.getParameter("username");
			String password = request.getParameter("password");
			try {
				if (book.login(username, password)) {
					// If they login, we want to save their cart
					UserBean usr = (UserBean) request.getSession().getAttribute("UserBean");
					Map<String, Integer> usrCart = usr.getCart().getCart();
					UserBean user = book.getUserBean(username);
					user.getCart().setCart(usrCart);
					request.getSession().setAttribute("UserBean", user);
					String street = book.getAddressAttribute(username, "street");
					String country = book.getAddressAttribute(username, "country");
					String province = book.getAddressAttribute(username, "province");
					String zip = book.getAddressAttribute(username, "zip");
					request.getSession().setAttribute("street", street);
					request.getSession().setAttribute("country", country);
					request.getSession().setAttribute("province", province);
					request.getSession().setAttribute("zip", zip);
					System.out.println("Welcome back, " + username);
					request.getSession().setAttribute("isLoggedIn", true);
					request.getSession().setAttribute("userName", user.getUserName());// delete when log out

					if (request.getParameter("fromPayment") != null) {
						request.getRequestDispatcher("/payment.jspx").forward(request, response);
					} else {
						request.getRequestDispatcher("/home.jspx").forward(request, response);
					}
				} else {
					// throw error incorrect password/authentication failed
					System.out.println("Incorrect password");
					request.getRequestDispatcher("/login.jspx").forward(request, response);
				}
			} catch (NoSuchAlgorithmException | SQLException e) {
				e.printStackTrace();
			}

		} else if (request.getParameter("login_admin") != null && request.getParameter("login_admin").equals("true")) { // Login
			// button
			// handler
			String username = request.getParameter("username");
			String password = request.getParameter("password");
			try {
				if (username.contentEquals("admin")) {
					if (book.login(username, password)) {
						UserBean user = book.getUserBean(username);
						request.getSession().setAttribute("UserBean", user);
						String street = book.getAddressAttribute(username, "street");
						String country = book.getAddressAttribute(username, "country");
						String province = book.getAddressAttribute(username, "province");
						String zip = book.getAddressAttribute(username, "zip");
						request.getSession().setAttribute("street", street);
						request.getSession().setAttribute("country", country);
						request.getSession().setAttribute("province", province);
						request.getSession().setAttribute("zip", zip);
						System.out.println("Welcome back, " + username);
						request.getSession().setAttribute("isLoggedIn", true);
						request.getSession().setAttribute("userName", user.getUserName());// delete when log out
						request.getRequestDispatcher("/home.jspx").forward(request, response);
					} else {
						System.out.println("Wrong password");
						request.getRequestDispatcher("/admin_login.jspx").forward(request, response);
					}
				} else { // throw error incorrect password/authentication failed
					System.out.println("Not an admin");
					request.getRequestDispatcher("/home.jspx").forward(request, response);
				}
			} catch (NoSuchAlgorithmException | SQLException e) {
				e.printStackTrace();
			}

		} else if (request.getParameter("register") != null && request.getParameter("register").equals("true")) { // Login
			// button
			String fName = request.getParameter("fname");
			String lName = request.getParameter("lname");
			String username = request.getParameter("username");
			String email = request.getParameter("email");
			String password = request.getParameter("password");// handler
			String address = request.getParameter("address");
			String country = request.getParameter("country");
			String province = request.getParameter("province");
			String zip = request.getParameter("zip");
			try {
				if (book.registerUser(fName, lName, username, email, password)) {
					book.insertAddress(username, address, province, country, zip);
					request.getRequestDispatcher("/login.jspx").forward(request, response);
					System.out.println("Registered!");
				} else {
					// throw error incorrect password/authentication failed
					System.out.println("Username already exists!");
					request.getRequestDispatcher("/register.jspx").forward(request, response);
				}
			} catch (NoSuchAlgorithmException | SQLException e) {
				e.printStackTrace();
			}

		}

		else if (request.getParameter("logout") != null && request.getParameter("logout").equals("true")) { // Login
			// button
			UserBean loggedoutUser = new UserBean();
			UserBean currUser = (UserBean) request.getSession().getAttribute("UserBean");
			loggedoutUser.getCart().setCart(currUser.getCart().getCart());
			request.getSession().setAttribute("UserBean", loggedoutUser);
			request.getSession().removeAttribute("isLoggedIn");
			request.getSession().removeAttribute("userName");
			request.getRequestDispatcher("/home.jspx").forward(request, response);

		} else if (request.getParameter("checkout") != null && request.getParameter("checkout").equals("true")) { // Login
			// button
			UserBean currUser = (UserBean) request.getSession().getAttribute("UserBean");
			String genCheckoutCart = "";
			try {
				genCheckoutCart = currUser.getCart().generateCheckoutCartHTML();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			request.getSession().setAttribute("genCheckoutHTML", genCheckoutCart);
			request.getRequestDispatcher("payment.jspx").forward(request, response);

		} 
		else if (request.getParameter("placeOrder") != null && request.getParameter("checkout").equals("true")) { // Login
			// button
			request.getRequestDispatcher("payment.jspx").forward(request, response);

		}
		else if (request.getParameter("updateCart") != null && request.getParameter("updateCart").equals("true")) { // Login
			// button
			UserBean user = (UserBean) request.getSession().getAttribute("UserBean");
			int itemsinCart = user.getCart().getCart().size();
			String genCart = "";
			String quantityParam = "";
			String bidParam = "";
			int paramNum = 0;
			System.out.println(itemsinCart);

			for (int i = 0; i < itemsinCart; i++) {
				System.out.println("Iterations = " + i + "items in cart = " + itemsinCart);
				paramNum = i + 1;
				bidParam = "bid" + paramNum + "quant";
				quantityParam = "item" + paramNum + "quant";

				bidParam = request.getParameter(bidParam);
				quantityParam = request.getParameter(quantityParam);

				user.getCart().updateQuantity(bidParam, Integer.parseInt(quantityParam));
			}
			request.getSession().setAttribute("CartNum", user.getCart().getTotalQuantity());
			request.getSession().setAttribute("UserBean", user);

			try {

				genCart = user.getCart().generateCartHTML();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			request.getSession().setAttribute("genCartHTML", genCart);

			request.getRequestDispatcher("cart.jspx").forward(request, response);

		} else if (request.getParameter("update") != null && request.getParameter("update").equals("true")) { // Login
			// button
			request.getRequestDispatcher("cart.jspx").forward(request, response);

		} else if (path != null) { // Page redirections based on request
			if (path.equals("/Login")) {
				request.getRequestDispatcher("/login.jspx").forward(request, response);
			} else if (path.equals("/Register")) {
				request.getRequestDispatcher("/register.jspx").forward(request, response);
			} else if (path.equals("/Cart")) {
				UserBean currUser = (UserBean) request.getSession().getAttribute("UserBean");
				String genCart = "";
				try {
					genCart = currUser.getCart().generateCartHTML();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				request.getSession().setAttribute("genCartHTML", genCart);
				request.getRequestDispatcher("/cart.jspx").forward(request, response);

			} else if (path.equals("/Payment")) {
				UserBean currUser = (UserBean) request.getSession().getAttribute("UserBean");
				String genCheckoutCart = "";
				try {
					genCheckoutCart = currUser.getCart().generateCheckoutCartHTML();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				request.getSession().setAttribute("genCheckoutHTML", "hi mom");
				request.getRequestDispatcher("/payment.jspx").forward(request, response);

			} else if (path.equals("/AdminLogin")) {

				request.getRequestDispatcher("/admin_login.jspx").forward(request, response);

			} else if (path.equals("/Admin")) {

				request.getRequestDispatcher("/admin.jspx").forward(request, response);

			} else {
				request.getRequestDispatcher("/home.jspx").forward(request, response);
			}

		} else if (request.getParameter("fetch") != null && request.getParameter("fetch").equals("true")) { // Ajax
																											// handling
																											// for home
																											// page
			String category = request.getParameter("category");
			try {

				Map<String, BookBean> rv = book.getBooksByCategory(category);
				String searchRes = book.generateBookCards(rv);
				request.setAttribute("library", searchRes);
				request.setAttribute("resultCount", rv.size());

				response.setContentType("text/html");
				response.getWriter().append(searchRes);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			request.getRequestDispatcher("/home.jspx").forward(request, response);
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

}
