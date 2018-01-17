package com.blu.imdg.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.cache.Cache;
import javax.cache.integration.CacheLoaderException;
import javax.cache.integration.CacheWriterException;
import org.apache.ignite.cache.store.CacheStore;
import org.apache.ignite.cache.store.CacheStoreAdapter;
import org.apache.ignite.cache.store.CacheStoreSession;
import org.apache.ignite.lang.IgniteBiInClosure;
import org.apache.ignite.resources.CacheStoreSessionResource;

import com.blu.imdg.jdbc.model.Person;

public class CacheJdbcPersonStore extends CacheStoreAdapter<Long, Person> {
    /** Store session. */
    @CacheStoreSessionResource
    private CacheStoreSession ses;

    /** {@inheritDoc} */
    @Override public Person load(Long key) {
    	System.out.println("*********************Load Method**********************");
        System.out.println(">>> Store load [key=" + key + ']');

        Connection conn = ses.attachment();

        try (PreparedStatement st = conn.prepareStatement("select * from PERSON where id = ?")) {
            st.setString(1, key.toString());

            ResultSet rs = st.executeQuery();

            return rs.next() ? new Person(rs.getLong(1), rs.getString(2), rs.getString(3)) : null;
        }
        catch (SQLException e) {
            throw new CacheLoaderException("Failed to load object [key=" + key + ']', e);
        }
    }

    /** {@inheritDoc} */
    @Override public void write(Cache.Entry<? extends Long, ? extends Person> entry) {
        Long key = entry.getKey();
        Person val = entry.getValue();

        System.out.println(">>> Store write [key=" + key + ", val=" + val + ']');

        try {
            Connection conn = ses.attachment();

            int updated;

            // Try update first. If it does not work, then try insert.
            // Some databases would allow these to be done in one 'upsert' operation.
            try (PreparedStatement st = conn.prepareStatement(
                "update PERSON set first_name = ?, last_name = ? where id = ?")) {
                st.setString(1, val.firstName);
                st.setString(2, val.lastName);
                st.setLong(3, val.id);

                updated = st.executeUpdate();
            }

            // If update failed, try to insert.
            if (updated == 0) {
                try (PreparedStatement st = conn.prepareStatement(
                    "insert into PERSON (id, first_name, last_name) values (?, ?, ?)")) {
                    st.setLong(1, val.id);
                    st.setString(2, val.firstName);
                    st.setString(3, val.lastName);

                    st.executeUpdate();
                }
            }
        }
        catch (SQLException e) {
            throw new CacheWriterException("Failed to write object [key=" + key + ", val=" + val + ']', e);
        }
    }

    /** {@inheritDoc} */
    @Override public void delete(Object key) {
        System.out.println(">>> Store delete [key=" + key + ']');

        Connection conn = ses.attachment();

        try (PreparedStatement st = conn.prepareStatement("delete from PERSON where id=?")) {
            st.setLong(1, (Long)key);

            st.executeUpdate();
        }
        catch (SQLException e) {
            throw new CacheWriterException("Failed to delete object [key=" + key + ']', e);
        }
    }

    /** {@inheritDoc} */
    @Override public void loadCache(IgniteBiInClosure<Long, Person> clo, Object... args) {
    	System.out.println("*********************LoadCache Method**********************");
        if (args == null || args.length == 0 || args[0] == null)
            throw new CacheLoaderException("Expected entry count parameter is not provided.");

        final int entryCnt = (Integer)args[0];

        /*Connection conn = ses.attachment();*/
        
        Connection conn;
		try {
			conn = DriverManager.getConnection(  
					"jdbc:mysql://localhost:3306/ignite?useSSL=false","root","root");
			
			 PreparedStatement stmt = conn.prepareStatement("select * from PERSON limit ?");
	        	try {
	            stmt.setInt(1, 1);

	            ResultSet rs = stmt.executeQuery();

	            int cnt = 0;

	            while (rs.next()) {
	                Person person = new Person(rs.getLong(1), rs.getString(2), rs.getString(3));
	                System.out.println(person);
	                System.out.println(person.id);
	                System.out.println("CLO : "+clo);
	                clo.apply(person.id, person);

	                cnt++;
	            }

	            System.out.println(">>> Loaded " + cnt + " values into cache.");
	        }
	        catch (SQLException e) {
	            throw new CacheLoaderException("Failed to load values from cache store.", e);
	        }
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

       
    }

	

	
}