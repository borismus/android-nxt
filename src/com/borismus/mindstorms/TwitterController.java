package com.borismus.mindstorms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Class handles incoming commands from twitter.
 * @author boris
 *
 */
public class TwitterController {
	private static final String TAG = MindstormsActivity.class.getName();

	// Twitter setup: account to listen for @replies
	final String twitterUser = "mindstorms";
	final String twitterPassword = "SECRET";
	Date lastCommandDate;
	
	public TwitterController() {
		getLastCommandTime();
	}

	/**
	 * Sets the last command time. Necessary to avoid re-executing previously
	 * issued commands. 
	 */
	public void getLastCommandTime() {
		String url = "http://search.twitter.com/search.json?q=@" + twitterUser;
		String json = getFeed(url);

		try {
			JSONObject obj = (JSONObject) new JSONTokener(json).nextValue();
			JSONArray results = obj.getJSONArray("results");

			lastCommandDate = getTweetDate(results.getJSONObject(0));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Helper for extracting the date out of a tweet JSON.
	 * @param item the JSON snippet corresponding to the tweet
	 * @return the date of the tweet
	 * @throws JSONException for malformed JSON
	 * @throws ParseException for malformed dates from twitter
	 */
	private Date getTweetDate(JSONObject item) throws JSONException,
			ParseException {
		String createdAt = item.getString("created_at");
		// Wed, 09 Jun 2010 17:47:19 +0000
		SimpleDateFormat df = new SimpleDateFormat(
				"EEE, dd MMM yyyy HH:mm:ss Z");
		return df.parse(createdAt);
	}

	/**
	 * Helper for extracting the command text out of a tweet's JSON.
	 * @param item the JSON snippet corresponding to the tweet
	 * @return the command
	 * @throws JSONException for malformed JSON
	 */
	private String getTweetCommand(JSONObject item) throws JSONException {
		String text = item.getString("text");
		// strip out the username from the start of the tweet
		return text.substring(twitterUser.length() + 1).trim();
	}

	/**
	 * Polls twitter and gets the latest command issued to the twitter user
	 * corresponding to the robot
	 * 
	 * @return latest command issued
	 */
	public String getLatestCommand() {
		String url = "http://search.twitter.com/search.json?q=@" + twitterUser;
		String json = getFeed(url);
		String command = null;
		try {
			JSONObject obj = (JSONObject) new JSONTokener(json).nextValue();
			JSONArray results = obj.getJSONArray("results");

			// for now, get the last thing in the feed
			JSONObject item = results.getJSONObject(0);
			Date date = getTweetDate(item);
			// only register the command if it occurred after the last command
			if (lastCommandDate != null && date.after(lastCommandDate)) {
				command = getTweetCommand(item);
				lastCommandDate = date;
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return command;
	}

	/**
	 * Helper for getting the contents (in String form) of a URL. 
	 * @param feedUrl the URL to access
	 * @return String contents of the feed
	 */
	private String getFeed(String feedUrl) {
		String feed = "";
		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet method = new HttpGet(feedUrl);
		method.addHeader("Pragma", "no-cache");
		HttpResponse response;
		try {
			response = client.execute(method);
			HttpEntity entity = response.getEntity();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					entity.getContent()));
			StringBuilder builder = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
			feed = builder.toString();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return feed;
	}
	
	/**
	 * Uploads a picture to twitpic. Involves a MultipartEntity for a complex 
	 * HTTP request (involves apache-mime4j and httpmime libraries)
	 * 
	 * @param data JPEG data in a byte array
	 * @param message the accompanying message
	 */
	public void uploadPicture(byte[] data, String message) {
		// took a picture. now upload it to twitpic.
		// http://twitpic.com/api/uploadAndPost
		DefaultHttpClient client = new DefaultHttpClient();
		HttpPost method = new HttpPost("http://twitpic.com/api/uploadAndPost");

		// multi-part: username, password, message, media (binary blob)
		MultipartEntity entity = new MultipartEntity();
		entity.addPart("media", new ByteArrayBody(data, "image/jpeg",
		"image.jpg"));
		try {
			entity.addPart("username", new StringBody(twitterUser,
					Charset.forName("UTF-8")));
			entity.addPart("password", new StringBody(twitterPassword, Charset
					.forName("UTF-8")));
			entity.addPart("message", new StringBody(message, Charset.forName("UTF-8")));
			method.setEntity(entity);
			HttpResponse response = client.execute(method);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
