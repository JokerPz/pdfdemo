package com.util;


import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.*;
import java.util.List;

/**
 * 分割PDF文件
 * 给定PDF文件和关键字，将其从PDF文件中提取(源文件不会少)出来，成为新的文件
 */
public class SplitUtil {
    public static void main(String[] args) throws IOException {
        //all.pdf你在resource目录下找得到，放到绝对路径就好了
        splitSpecifiedStudentPDF("D://all.pdf", "D://specifed.pdf", "ZHANG", "Name");
    }

    /**
     *
     * @param sourceFile 源文件
     * @param targetFile 目标生成文件
     * @param specfiedKeyword 指定查找关键字  如指定学号 1022304
     * @param keyword 辅助查找关键字  如查找下一个学号  “Student ID”
     * @return
     * @throws IOException
     */
    public static boolean splitSpecifiedStudentPDF(String sourceFile, String targetFile, String specfiedKeyword, String keyword) throws IOException {
        //接受文件，转化为流
        File pdfFile = new File(sourceFile);
        byte[] pdfData = new byte[(int) pdfFile.length()];

        FileInputStream fis = new FileInputStream(pdfFile);

        fis.read(pdfData);

        //获取学生出现页码
        List<float[]> specifiedPositions = GetKeyWordPosition.findKeywordPostions(pdfData, specfiedKeyword);

        System.out.println("size: " + specifiedPositions.size());
        System.out.println("本人所在页码: " + specifiedPositions.get(0)[0]);


        //获取所有学生出现所在页码，紧邻在其后的是下一个学生所在页码
        List<float[]> otherPositions = GetKeyWordPosition.findKeywordPostions(pdfData, keyword);

        System.out.println("size: " + otherPositions.size());

        //起始页码就是学生第一次出现的页码  结束节码就是下一个学生出现的页码-1
        int startPageNum = (int) specifiedPositions.get(0)[0];
        int endPageNum = getNextPage(specifiedPositions, otherPositions) -1;

        splitPDF(new FileInputStream(sourceFile), new FileOutputStream(targetFile), startPageNum, endPageNum);

        return true;
    }


    //获取指定学生的下一个紧邻的学生所在页码
    public static int getNextPage(List<float[]> specfiedPositions, List<float[]> otherPositions){

        for (float[] position : otherPositions) {

            if(position[0]>specfiedPositions.get(0)[0])
                return (int)position[0];
        }
        return -1;
    }


    /**
     * @param inputStream  PDF文件的输入流
     * @param outputStream PDF文件的输出流
     * @param fromPage     开始的页码   闭区间
     * @param toPage       结束的页码   闭区间
     */
    public static void splitPDF(InputStream inputStream,
                                OutputStream outputStream, int fromPage, int toPage) {
        Document document = new Document();
        try {
            PdfReader inputPDF = new PdfReader(inputStream);

            int totalPages = inputPDF.getNumberOfPages();

            //make fromPage equals to toPage if it is greater
            if (fromPage > toPage) {
                fromPage = toPage;
            }
            if (toPage > totalPages) {
                toPage = totalPages;
            }

            // Create a writer for the outputstream
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);

            document.open();
            PdfContentByte cb = writer.getDirectContent(); // Holds the PDF data
            PdfImportedPage page;

            while (fromPage <= toPage) {
                document.newPage();
                page = writer.getImportedPage(inputPDF, fromPage);
                cb.addTemplate(page, 0, 0);
                fromPage++;
            }
            outputStream.flush();
            document.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (document.isOpen())
                document.close();
            try {
                if (outputStream != null)
                    outputStream.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

}
