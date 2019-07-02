package edu.tigers.sumatra.statistics;

import static org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Connection parameters for an InfluxDB.
 */
public class InfluxDbConnectionParameters
{
	private String url;
	private String dbName;
	private String username;
	private String password;
	
	
	public void setUrl(final String url)
	{
		this.url = url;
	}
	
	
	public void setDbName(final String dbName)
	{
		this.dbName = dbName;
	}
	
	
	public void setUsername(final String username)
	{
		this.username = username;
	}
	
	
	public void setPassword(final String password)
	{
		this.password = password;
	}
	
	
	public String getUrl()
	{
		return url;
	}
	
	
	public String getDbName()
	{
		return dbName;
	}
	
	
	public String getUsername()
	{
		return username;
	}
	
	
	public String getPassword()
	{
		return password;
	}
	
	
	public boolean isComplete()
	{
		return StringUtils.isNotBlank(url)
				&& StringUtils.isNotBlank(dbName)
				&& StringUtils.isNotBlank(username)
				&& StringUtils.isNotBlank(password);
	}
	
	
	@Override
	public String toString()
	{
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("url", url)
				.append("dbName", dbName)
				.append("username", username)
				.append("password", StringUtils.isBlank(password) ? null : "*****")
				.toString();
	}
}
