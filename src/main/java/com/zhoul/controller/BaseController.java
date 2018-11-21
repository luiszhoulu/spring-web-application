package com.zhoul.controller;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by Administrator on 15-1-31.
 */
/*There must be a Controller annotation or the application will doesn't work .*/
@Controller
public class BaseController {
    private static int counter = 0;
    private static final String VIEW_INDEX = "index";
    private static final Logger logger = LoggerFactory.getLogger(BaseController.class);

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String welcome(ModelMap model) {
        model.addAttribute("message", "Welcome");
        model.addAttribute("counter", ++counter);
        logger.debug("[Welcome counter :{}", counter);
        return VIEW_INDEX;//返回index.jsp
    }

    @RequestMapping(value = "/{name}", method = RequestMethod.GET)
    public String welcome(@PathVariable String name, ModelMap model) {
        model.addAttribute("message", "Welcome " + name);
        model.addAttribute("counter", ++counter);
        logger.debug("[Welcome counter :{}", counter);
        return VIEW_INDEX;//返回index.jsp
    }
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(@RequestParam String id, HttpServletResponse response) throws IOException {
        //1.设置文件ContentType类型，这样设置，会自动判断下载文件类型
        response.setContentType("multipart/form-data");
        String filename = URLEncoder.encode("压缩文件", "UTF-8");
        //2.设置文件头：最后一个参数是设置下载文件名(假如我们叫a.pdf)
        response.setHeader("Content-Disposition", "attachment;fileName="+ filename + ".zip");
        ServletOutputStream out = response.getOutputStream();
        File file = new File("C:\\Users\\Luis\\Documents\\行程单.pdf ");
        try (FileInputStream inputStream = new FileInputStream(file);
             ZipArchiveOutputStream zos = new ZipArchiveOutputStream(out)){

            //3.通过response获取ServletOutputStream对象(out)
            ZipArchiveEntry zipEntry = new ZipArchiveEntry("大都会(だいとかい)に 仆(ぼく)/newFileName.pdf");
            zos.putArchiveEntry(zipEntry);

            int b = 0;
            byte[] buffer = new byte[512];
            while (b != -1){
                b = inputStream.read(buffer);
                //4.写到输出流(out)中
                zos.write(buffer,0,b);
            }

            zos.setEncoding("MS932");
        }
        logger.debug("[Welcome counter :{}", counter);
    }

    @RequestMapping(value = "/compressAndWriteToDisk", method = RequestMethod.GET)
    public void compressAndWriteToDisk(HttpServletResponse response) throws Exception {
        createZip("C:\\Users\\Luis\\Documents\\EuropeTrip - Copy", "C:\\Users\\Luis\\Documents\\output.zip");
    }
    public ZipOutputStream createZip(String baseDir, String objFileName) throws Exception {
        File folderObject = new File(baseDir);


        if (folderObject.exists()) {
            List fileList = getSubFiles(new File(baseDir));


            //压缩文件名
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(objFileName));


            ZipEntry ze = null;
            byte[] buf = new byte[1024];
            int readLen = 0;
            for (int i = 0; i < fileList.size(); i++) {
                File f = (File) fileList.get(i);
                System.out.println("Adding: " + f.getPath() + f.getName());


                //创建一个ZipEntry，并设置Name和其它的一些属性
                ze = new ZipEntry(getAbsFileName(baseDir, f));
                ze.setSize(f.length());
                ze.setTime(f.lastModified());


                //将ZipEntry加到zos中，再写入实际的文件内容
                zos.putNextEntry(ze);
                InputStream is = new BufferedInputStream(new FileInputStream(f));
                while ((readLen = is.read(buf, 0, 1024)) != -1) {
                    zos.write(buf, 0, readLen);
                }
                is.close();
                System.out.println("done...");
            }
            return zos;
        } else {
            throw new Exception("this folder isnot exist!");
        }
    }

    public InputStream createZipToInputStream(String baseDir) throws Exception {
        File folderObject = new File(baseDir);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream( bos );
        if (folderObject.exists()) {
            List fileList = getSubFiles(new File(baseDir));
            ZipEntry ze = null;
            byte[] buf = new byte[1024];
            int readLen = 0;
            for (int i = 0; i < fileList.size(); i++) {
                File f = (File) fileList.get(i);


                ze = new ZipEntry(getAbsFileName(baseDir, f));
                ze.setSize(f.length());
                ze.setTime(f.lastModified());


                zos.putNextEntry(ze);
                InputStream is = new BufferedInputStream(new FileInputStream(f));
                while ((readLen = is.read(buf, 0, 1024)) != -1) {
                    zos.write(buf, 0, readLen);
                }
                is.close();
            }
            zos.close();
            return new ByteArrayInputStream(bos.toByteArray());
        } else {
            throw new Exception("this folder isnot exist!");
        }
    }

    private ZipEntry getAbsFileName(String baseDir, File f) {
        return  new ZipEntry( f.getName());
    }

    private List<File> getSubFiles(File file) {
        File[] fileList = file.listFiles();
        List<File> wjjList = new ArrayList<File>();//新建一个文件夹集合
        for (int i = 0; i < fileList.length; i++) {
            if (fileList[i].isFile()) {//判断是否为文件夹
                wjjList .add(fileList[i]);
            }
        }
        return  wjjList;
    }

    @RequestMapping(value = "/compressAndDownload", method = RequestMethod.GET)
    public void compressAndDownload(HttpServletResponse response) throws IOException {
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment;fileName=file.zip");
        createZipAndDownload(response.getOutputStream(), "C:\\Users\\Luis\\Documents\\EuropeTrip - Copy");
    }

    private void createZipAndDownload(ServletOutputStream outputStream, String baseDir) throws IOException {
        ZipOutputStream zos = new ZipOutputStream(outputStream);
        File folderObject = new File(baseDir);


        if (folderObject.exists()) {
            List fileList = getSubFiles(new File(baseDir));


            ZipEntry ze = null;
            byte[] buf = new byte[1024];
            int readLen = 0;
            for (int i = 0; i < fileList.size(); i++) {
                File f = (File) fileList.get(i);
                System.out.println("Adding: " + f.getPath() + f.getName());


                //创建一个ZipEntry，并设置Name和其它的一些属性
                ze = new ZipEntry("/tempFolder"+ UUID.randomUUID()+"/" + getAbsFileName(baseDir, f));
                ze.setSize(f.length());
                ze.setTime(f.lastModified());


                //将ZipEntry加到zos中，再写入实际的文件内容
                zos.putNextEntry(ze);
                InputStream is = new BufferedInputStream(new FileInputStream(f));
                while ((readLen = is.read(buf, 0, 1024)) != -1) {
                    zos.write(buf, 0, readLen);
                }
                is.close();
            }
        } else {
            try {
                throw new Exception("this folder isnot exist!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        zos.close();
    }

    @RequestMapping(value = "/receiveInputStreamAndDownload", method = RequestMethod.GET)
    public void receiveInputStreamAndDownload(HttpServletResponse response) throws Exception {
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment;fileName=file.zip");
        ZipOutputStream zos = createZip("C:\\Users\\Luis\\Documents\\EuropeTrip - Copy", "C:\\Users\\Luis\\Documents\\output.zip");

        List<InputStream> inputs = Arrays.asList(createZipToInputStream("C:\\Users\\Luis\\Documents\\EuropeTrip - Copy"), createZipToInputStream("C:\\Users\\Luis\\Documents\\EuropeTrip - Copy"),
                createZipToInputStream("C:\\Users\\Luis\\Documents\\EuropeTrip - Copy"));
        writeInputStreamsToResponse(inputs, response.getOutputStream());
    }

    private InputStream modifyFileNameInInputStream(InputStream input) {
        ZipInputStream stream = new ZipInputStream(input);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(bos);
        ZipEntry entry;
        int i=10;
        try {
            while ((entry = stream.getNextEntry()) != null) {
                // We can read the file information from the ZipEntry.
                String fileInfo = String.format("Entry: [%s] len %d added %TD",
                        entry.getName(), entry.getSize(),
                        new Date(entry.getTime()));

                ZipEntry ze = null;
                byte[] buf = new byte[1024];
                int readLen = 0;

                ze = new ZipEntry("newName"+ (i++) +".pdf");
                ze.setTime(entry.getLastModifiedTime().toMillis());


                //将ZipEntry加到zos中，再写入实际的文件内容
                zos.putNextEntry(ze);
                while ((readLen = stream.read(buf, 0, 1024)) != -1) {
                    zos.write(buf, 0, readLen);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            stream.close();
            zos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ByteArrayInputStream(bos.toByteArray());
    }

    private void writeInputStreamsToResponse(List<InputStream> inputs, ServletOutputStream outputStream) throws Exception {
        ZipOutputStream zos = new ZipOutputStream(outputStream);
        ZipEntry ze = null;
        byte[] buf = new byte[1024];
        int readLen = 0;
        int i = 0;
        for (; i < inputs.size(); i++) {
            //创建一个ZipEntry，并设置Name和其它的一些属性
            ze = new ZipEntry("/tempFolder" + UUID.randomUUID() + "/" + i+".zip");
            InputStream is = inputs.get(i);

            is = modifyFileNameInInputStream(is);
            //将ZipEntry加到zos中，再写入实际的文件内容
            zos.putNextEntry(ze);
            // while ((readLen = is.read(buf, 0, 1024)) != -1) {
            // zos.write(buf, 0, readLen);
            int inb;
            while ((inb = is.read()) != -1){
                zos.write(inb);
            }
        }
        zos.close();
    }



    private ZipOutputStream getCompressedFileStream(File file, ServletOutputStream out) throws IOException {
        ZipOutputStream zos = new ZipOutputStream(out);
        ZipEntry ze = null;
        byte[] buf = new byte[1024];
        int readLen = 0;

        //创建一个ZipEntry，并设置Name和其它的一些属性
        ze = new ZipEntry(file.getName());
        ze.setSize(file.length());
        ze.setTime(file.lastModified());


        //将ZipEntry加到zos中，再写入实际的文件内容
        zos.putNextEntry(ze);
        InputStream is = new BufferedInputStream(new FileInputStream(file));
        while ((readLen = is.read(buf, 0, 1024)) != -1) {
            zos.write(buf, 0, readLen);
        }
        is.close();
        return zos;
    }
}