package edu.tigers.sumatra.statistics;

import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import static org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE;


/**
 * Connection parameters for an InfluxDB.
 */
@Data
public class InfluxDbConnectionParameters
{
	private String url;
	private String dbName;
	private String username;
	private String password;


	public boolean isComplete()
	{
		return StringUtils.isNotBlank(url)
				&& StringUtils.isNotBlank(dbName);
	}


	public boolean hasAuth()
	{
		return StringUtils.isNotBlank(username)
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
