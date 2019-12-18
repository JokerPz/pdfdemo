package com.util;


import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.*;

import java.io.*;
import java.util.List;

import static com.util.GetKeyWordPosition.findKeywordPostions;

/**
 * 印章工具类
 * 使用findKeyWordAndAddImage寻找指定关键字，并且在其所处位置盖章
 */
public class StampUtil {
    public static void main(String[] args) throws Exception {
        //pt3.pdf你在resource目录下找得到，放到绝对路径下就好了
        findKeyWordAndAddImage("D://pt3.pdf", "src/main/resources/answer2/utils.pdf", "上海", "src/main/resources/yz.gif", -20, -100);
    }

    /**
     * 寻找指定的关键字后给其所在位置盖章
     * @param source    源pdf文件
     * @param target    目标pdf文件
     * @param keyword   关键字
     * @param image     印章路径
     * @param xOffset   x轴偏移量(没有则指定为0)
     * @param yOffset   y轴偏移量(没有则指定为0)
     * @return          返回结果
     * @throws IOException
     * @throws DocumentException
     */
    public static boolean findKeyWordAndAddImage(String source, String target, String keyword, String image, float xOffset, float yOffset) throws IOException, DocumentException {
        boolean result = false;

        File pdfFile = new File(source);
        byte[] pdfData = new byte[(int) pdfFile.length()];

        try (FileInputStream fis = new FileInputStream(pdfFile)) {
            fis.read(pdfData);

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("文件不存在");
            return result;
        }

        //查到关键字返回位置
        List<float[]> positions = findKeywordPostions(pdfData, keyword);
        if(positions.size() == 0){
            System.out.println("关键字不存在");
            return result;
        }

        //添加水印 //会查询到多个关键字定位，固定取最后一个
        result = addImage(source, target,  image, positions.get(positions.size()-1), xOffset, yOffset);
        return true;
    }



    //添加水印(签章)
    private static boolean addImage(String source, String target, String imagePath, float[] positions, float xOffset, float yOffset) throws IOException, DocumentException {
        // 读取模板文件
        InputStream input = new FileInputStream(new File(source));
        PdfReader reader = new PdfReader(input);
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(target));


        // 提取pdf中的表单
        AcroFields form = stamper.getAcroFields();
        form.addSubstitutionFont(BaseFont.createFont("STSong-Light","UniGB-UCS2-H", BaseFont.NOT_EMBEDDED));

        // 读图片
        Image image = Image.getInstance(imagePath);

        // 获取操作的页面
        PdfContentByte under = stamper.getOverContent((int)positions[0]);

        //设置图片的定位
        image.setAbsolutePosition(positions[1]+xOffset, positions[2]+yOffset);


        //image.scalePercent(75); //缩放图片为指定百分比

        // 设置透明度为0.8
        PdfGState gs = new PdfGState();

        gs.setFillOpacity(0.8f);
        under.setGState(gs);
        // 添加图片
        under.addImage(image);


        stamper.close();
        reader.close();

        return true;
    }

}
