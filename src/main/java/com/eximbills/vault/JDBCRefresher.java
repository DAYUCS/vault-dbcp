package com.eximbills.vault;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.response.DatabaseResponse;
import com.zaxxer.hikari.HikariConfigMXBean;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;

public class JDBCRefresher implements Runnable {

	private static VaultConfig vaultConfig;

	private static Vault vault;

	public JDBCRefresher() {
		try {
			vaultConfig = new VaultConfig().address("http://localhost:8200")
					.token("00000000-0000-0000-0000-000000000000").build();
			vault = new Vault(vaultConfig);
		} catch (VaultException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		System.out.println("Vault Refresher trigged by scheduler");
		String username = "";
		String password = "";
		try {
			DatabaseResponse databaseResponse = vault.database().creds("readonly");
			System.out.println("Vault response: " + databaseResponse.getLeaseId());
			System.out.println("Username: " + databaseResponse.getCredential().getUsername());
			System.out.println("Password: " + databaseResponse.getCredential().getPassword());
			System.out.println("Lease Duration: " + databaseResponse.getLeaseDuration());
			username = databaseResponse.getCredential().getUsername();
			password = databaseResponse.getCredential().getPassword();
		} catch (VaultException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Context ctx;
		try {
			ctx = new InitialContext();
			HikariDataSource ds = (HikariDataSource) ctx.lookup("java:/comp/env/jdbc/postgres");
			
			System.out.println("Set new username and password");
			HikariConfigMXBean hikariconfigMXBean = ds.getHikariConfigMXBean();
            hikariconfigMXBean.setUsername(username);
            hikariconfigMXBean.setPassword(password);
            
            System.out.println("Evict connections");
            HikariPoolMXBean hikariPoolMXBean = ds.getHikariPoolMXBean();
            if (hikariPoolMXBean != null) {
                hikariPoolMXBean.softEvictConnections();
            }
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
