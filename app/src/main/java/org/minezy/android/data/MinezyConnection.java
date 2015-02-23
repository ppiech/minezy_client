package org.minezy.android.data;

import android.content.Context;
import android.preference.PreferenceManager;

import org.minezy.android.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;

import javax.inject.Inject;

import static java.util.logging.Logger.getLogger;

public class MinezyConnection {

    public static class MinezyConnectionException extends Exception {
        public MinezyConnectionException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static final int TIMEOUT = 30000;

    @Inject
    Context mContext;

    public MinezyConnection(Context context) {
        mContext = context;
    }

    private String getServerAddress() {
        return PreferenceManager.getDefaultSharedPreferences(mContext)
            .getString(mContext.getString(R.string.pref_server_address),
                mContext.getString(R.string.pref_default_server_address));
    }


    public String requestGet(String path) throws MinezyConnectionException {
        String url = getServerAddress() + path;
        getLogger(getClass().getName()).log(Level.FINE, url);
        try {
            URL u = new URL(url);
            HttpURLConnection c = (HttpURLConnection) u.openConnection();
            c.setRequestMethod("GET");
            c.setConnectTimeout(TIMEOUT);
            c.setReadTimeout(TIMEOUT);
            c.connect();

            if (c.getResponseCode() == 200 || c.getResponseCode() == 201) {
                BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();
                String response = sb.toString();
                getLogger(getClass().getName()).log(Level.FINE, response);
                return sb.toString();
            } else {
                throw new MinezyConnectionException(
                    Integer.toString(c.getResponseCode()) + " response when processing: " + url, null);
            }

        } catch (MalformedURLException e) {
            throw new MinezyConnectionException("Error processing: " + url, e);
            //getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } catch (IOException e) {
            throw new MinezyConnectionException("Error processing: " + url, e);
        }
    }

}
