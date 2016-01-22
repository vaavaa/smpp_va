package kz.smpp.client;

import kz.smpp.jsoup.ParseHtml;
import kz.smpp.mysql.MyDBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Calendar;

public class FeedContentTask implements  Runnable {

	public static final Logger log = LoggerFactory.getLogger(FeedContentTask.class);
    MyDBConnection mDBConnection;
	public FeedContentTask(MyDBConnection mDBConn) {
        mDBConnection = mDBConn;
	}

	@Override
	public void run() {
        Calendar cal = Calendar.getInstance();
        int currentHour = cal.get(Calendar.HOUR);
        if (currentHour > 23) {
            //then rock on
            mDBConnection.rate();
            log.debug("Done. DB is updated with rate");

            mDBConnection.ascendant();
            log.debug("Done. DB is updated with ascendant");

            mDBConnection.metcast();
            log.debug("Done. DB is updated with metcast");

            ParseHtml phtml = new ParseHtml(mDBConnection.getSettings("anecdote"));
            phtml.close();
            log.debug("Done. DB is updated with anecdote");
        }
	}
}
