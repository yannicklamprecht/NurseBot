package asylum.nursebot.persistence;

import java.sql.Connection;

import org.javalite.activejdbc.Base;

public class Connector {

    private static final long CONNECTION_SLEEP = 30 * 1000;
    private Connection connection;
    private String host;
    private String schema;
    private String user;
    private String password;

    private void connect() {
        Base.open(
        "com.mysql.cj.jdbc.Driver",
        "jdbc:mysql://" + host + "/" + schema + "?useUnicode=true" +
        "&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false" +
        "&serverTimezone=UTC", user, password);
        connection = Base.connection();
    }

    public Connector(String host, String schema, String user, String password) {
        this.host = host;
        this.schema = schema;
        this.user = user;
        this.password = password;

        connect();

        new Thread(() -> {
            Base.attach(connection);

            while (true) {
                try {
                    Thread.sleep(CONNECTION_SLEEP);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                try {
                    Base.exec("SELECT 1");
                } catch (Exception e1) {
                    try {
                        Base.close();
                    } catch (Exception e2) {
                    }
                    connect();
                }
            }
        }).start();

        Base.detach();
    }

    public void connectThread() {
        Base.attach(connection);
    }

    public void disconnectThread() {
        Base.detach();
    }

    public void close() {
        Base.close();
    }
}
