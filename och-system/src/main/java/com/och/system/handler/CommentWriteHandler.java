package com.och.system.handler;


import com.alibaba.excel.write.handler.context.CellWriteHandlerContext;
import com.alibaba.excel.write.style.AbstractCellStyleStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

import java.util.List;

/**
 * @author danmo
 * @date 2025/06/16 19:00
 */
@Slf4j
public class CommentWriteHandler extends AbstractCellStyleStrategy {

    private List<String> headNameList;
    private String commentStr;

    public CommentWriteHandler(List<String> headNameList, String commentStr) {
        this.headNameList = headNameList;
        this.commentStr = commentStr;
    }

    @Override
    protected void setHeadCellStyle(CellWriteHandlerContext context) {
        Cell cell = context.getCell();
        if (headNameList.contains(cell.getStringCellValue())) {
            Sheet sheet = context.getWriteSheetHolder().getSheet();
            Drawing<?> drawingPatriarch = sheet.createDrawingPatriarch();
            Comment comment = drawingPatriarch.createCellComment(new XSSFClientAnchor(0, 0, 0, 0, cell.getColumnIndex(), 0, cell.getColumnIndex() + 1, 1));
            comment.setString(new XSSFRichTextString(commentStr));
            cell.setCellComment(comment);
        }
    }
}
