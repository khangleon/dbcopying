package vn.com.sky.view.image;

import static java.nio.file.StandardOpenOption.READ;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import vn.com.sky.common.Lib;
import vn.com.sky.dao.his.ImagingDocument;
import vn.com.sky.view.servlet.BaseServlet;

@WebServlet(name = "imagingvideo", urlPatterns = { "/imagingvideo" })
public class ImagingVideoServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;
    private static final int BUFFER_LENGTH = 1024 * 1024;
    private static final long EXPIRE_TIME = 1000 * 60 * 60 * 24;
    private static final Pattern RANGE_PATTERN = Pattern.compile("bytes=(?<start>\\d*)-(?<end>\\d*)");

    public ImagingVideoServlet() {
        super();
    }

    @Override
    protected void doPostGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        processRequest(req, res);
    }

    private void processRequest(final HttpServletRequest request, final HttpServletResponse response)
            throws IOException {
        Long id = Lib.toLong(request.getParameter("id"), -1);
        String videoPath = null;
        String videoFilename = null;

        ImagingDocument imagingImage = findEntity(ImagingDocument.class, id);
        if (imagingImage != null) {
            videoPath = imagingImage.getDirpath() + File.separator + imagingImage.getFilepath() + File.separator;
            videoFilename = imagingImage.getFilename();
        }

        Path video = null;
        try {
            video = Paths.get(videoPath, videoFilename);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int length = (int) Files.size(video);
        int start = 0;
        int end = length - 1;

        String range = request.getHeader("Range");
        Matcher matcher = RANGE_PATTERN.matcher(range);

        if (matcher.matches()) {
            String startGroup = matcher.group("start");
            start = startGroup.isEmpty() ? start : Integer.valueOf(startGroup);
            start = start < 0 ? 0 : start;

            String endGroup = matcher.group("end");
            end = endGroup.isEmpty() ? end : Integer.valueOf(endGroup);
            end = end > length - 1 ? length - 1 : end;
        }

        int contentLength = end - start + 1;

        response.reset();
        response.setBufferSize(BUFFER_LENGTH);
        response.setHeader("Content-Disposition", String.format("inline;filename=\"%s\"", videoFilename));
        response.setHeader("Accept-Ranges", "bytes");
        response.setDateHeader("Last-Modified", Files.getLastModifiedTime(video).toMillis());
        response.setDateHeader("Expires", System.currentTimeMillis() + EXPIRE_TIME);
        response.setContentType(Files.probeContentType(video));
        response.setHeader("Content-Range", String.format("bytes %s-%s/%s", start, end, length));
        response.setHeader("Content-Length", String.format("%s", contentLength));
        response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);

        int bytesRead;
        int bytesLeft = contentLength;
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_LENGTH);

        try (SeekableByteChannel input = Files.newByteChannel(video, READ);
                OutputStream output = response.getOutputStream()) {

            input.position(start);

            while ((bytesRead = input.read(buffer)) != -1 && bytesLeft > 0) {
                buffer.clear();
                output.write(buffer.array(), 0, bytesLeft < bytesRead ? bytesLeft : bytesRead);
                bytesLeft -= bytesRead;
            }
        }
    }
}
