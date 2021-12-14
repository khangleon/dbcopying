package vn.com.sky.view.image;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import vn.com.sky.view.servlet.BaseServlet;

public class VideoServlet extends BaseServlet {
    private static final long serialVersionUID = 2L;
    private String contentType = "video/webm";

    public VideoServlet() {
        super();
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        doPostGet(req, res);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        doPostGet(req, res);
    }

    protected void doPostGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        OutputStream out = res.getOutputStream();
        InputStream in = null;
        try {
            in = getInputStream(req);
            byte[] buffer = new byte[2096];
            int read = 0;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                in.close();
            }
            out.flush();
            out.close();
        }
    }

    public InputStream getInputStream(HttpServletRequest req) {
        return null;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}

// VideoServlet.java

