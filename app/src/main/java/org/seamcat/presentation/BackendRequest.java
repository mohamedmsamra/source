package org.seamcat.presentation;

public class BackendRequest {

    private static final String BACKEND_URL = "http://www.seamcat.org/seamcat-backend-1.0.0/seamcat/news/latest";
    private static final String VERSION_URL = "http://www.seamcat.org/seamcat-backend-1.0.0/seamcat/version";

    public static String requestNews( String versionString ) {
        throw new RuntimeException("Not supported");

        /*JSONHandler handler = new JSONHandler(BACKEND_URL);
        handler.header( "x-seamcat-language",     System.getProperty( "user.language" ) )
                .header("x-seamcat-os", System.getProperty("os.name"))
                .header("x-seamcat-java-version", System.getProperty("java.version"))
                .header("x-seamcat-version", versionString);
        LatestNewsItems news = handler.read(LatestNewsItems.class);


        String image = ImageLoader.class.getResource("news_16x16.png").toString();
        final StringBuilder sb = new StringBuilder( " ");
        for (String item : news.getItems()) {
            sb.append(" <img src=\"").append( image ).append("\">").append(" ").append( item );
        }
        return sb.toString();*/
    }

    public static Version requestVersion() {
        throw new RuntimeException("Not supported");

        //return new JSONHandler(VERSION_URL).read( Version.class );
    }
}
