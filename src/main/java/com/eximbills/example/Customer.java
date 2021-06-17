package com.eximbills.example;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.zaxxer.hikari.HikariDataSource;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class Customer
 */
public class Customer extends HttpServlet {

	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Context ctx = null;
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			ctx = new InitialContext();
			HikariDataSource ds = (HikariDataSource) ctx.lookup("java:/comp/env/jdbc/postgres");

			con = ds.getConnection();
			stmt = con.prepareStatement("select id, name, address from customer");

			rs = stmt.executeQuery();

			PrintWriter out = response.getWriter();
			response.setContentType("text/html");
			out.print("<html><body><h2>Customer Details</h2>");
			out.print("<table border=\"1\" cellspacing=10 cellpadding=5>");
			out.print("<th>Customer ID</th>");
			out.print("<th>Customer Name</th>");
			out.print("<th>Customer Address</th>");

			while (rs.next()) {
				out.print("<tr>");
				out.print("<td>" + rs.getInt("id") + "</td>");
				out.print("<td>" + rs.getString("name") + "</td>");
				out.print("<td>" + rs.getString("address") + "</td>");
				out.print("</tr>");
			}
			out.print("</table></body><br/>");

			// lets print some DB information
			out.print("<h3>Database Details</h3>");
			out.print("Database Product: " + con.getMetaData().getDatabaseProductName() + "<br/>");
			out.print("Database Driver: " + con.getMetaData().getDriverName());
			out.print("</html>");

		} catch (NamingException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				stmt.close();
				con.close();
				ctx.close();
			} catch (SQLException e) {
				System.out.println("Exception in closing DB resources");
			} catch (NamingException e) {
				System.out.println("Exception in closing Context");
			}

		}
	}

}
