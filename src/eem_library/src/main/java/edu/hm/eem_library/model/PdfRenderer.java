package edu.hm.eem_library.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.ParcelFileDescriptor;

import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;
import com.shockwave.pdfium.util.Size;

import java.io.IOException;


/** Wrapper class for {@link PdfiumCore} to make it behave similar to {@link android.graphics.pdf.PdfRenderer}.
 * {@link android.graphics.pdf.PdfRenderer} does not work correctly on Devices with API lower
 * than Oreo.
 */
public class PdfRenderer {
    private final PdfiumCore pdfiumCore;
    private final PdfDocument pdfDocument;

    public PdfRenderer(Context context, ParcelFileDescriptor fd) throws IOException {
        this.pdfiumCore = new PdfiumCore(context);
        this.pdfDocument = pdfiumCore.newDocument(fd);
    }

    public void close(){
        pdfiumCore.closeDocument(pdfDocument);
    }

    public int getPageCount(){
        return pdfiumCore.getPageCount(pdfDocument);
    }

    public Page openPage(int pageNum){
        return new Page(pageNum);
    }

    public class Page {
        private final int pageNum;

        private Page(int pageNum) {
            pdfiumCore.openPage(pdfDocument, pageNum);
            this.pageNum = pageNum;
        }

        public void render(Bitmap bitmap){
            pdfiumCore.openPage(pdfDocument, pageNum);
            Size s = pdfiumCore.getPageSize(pdfDocument, pageNum);
            float scaler = ((float)bitmap.getWidth())/s.getWidth();
            int height = (int) (s.getHeight()*scaler);
            int startY = bitmap.getHeight()/2-height/2;
            pdfiumCore.renderPageBitmap(pdfDocument, bitmap, pageNum, 0, startY, bitmap.getWidth(), height);
        }

        public void close(){

        }
    }
}

