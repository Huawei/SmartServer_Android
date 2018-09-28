package com.huawei.smart.server.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintPdfTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.view.Gravity;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.Toast;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ImageUtils;
import com.flipboard.bottomsheet.BottomSheetLayout;
import com.flipboard.bottomsheet.commons.MenuSheetView;
import com.huawei.smart.server.BaseActivity;
import com.huawei.smart.server.R;

import org.slf4j.LoggerFactory;

import java.io.File;

import butterknife.BindView;
import butterknife.OnClick;

import static com.huawei.smart.server.HWConstants.BUNDLE_KEY_REPORT_HTML;

/**
 * 显示报告
 */
    public class DisplayReportActivity extends BaseActivity {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(DisplayReportActivity.class.getSimpleName());

    private static final String REPORT_FOLDER = "report";

    @BindView(R.id.report)
    WebView browser;
    @BindView(R.id.bottom_sheet)
    BottomSheetLayout bottomSheetLayout;

    String html;
    MenuSheetView selectShareFileTypeSheet;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WebView.enableSlowWholeDocumentDraw();
        setContentView(R.layout.activity_display_report);
        this.initialize(R.string.title_display_report, true);
        this.html = getExtraString(BUNDLE_KEY_REPORT_HTML);
        browser.getSettings().setDefaultTextEncodingName("utf-8");
        browser.loadData(html, "text/html; charset=utf-8", "utf-8");
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void shareAsImage() {
        Bitmap bitmap = getScreenShootBitmap();
        Bitmap compressed = ImageUtils.compressByQuality(bitmap, 70);
        try {
            String fileDir = getFilesDir().getPath() + File.separator + REPORT_FOLDER;
            FileUtils.createOrExistsDir(fileDir);
            FileUtils.deleteFilesInDir(fileDir);

            String filepath = fileDir + File.separator + System.currentTimeMillis() + ".jpg";
            ImageUtils.save(compressed, filepath, Bitmap.CompressFormat.JPEG);
            // Log.i("Share", "File size is :" + FileUtils.getFileSize(filepath));
            share(this, filepath, "image/jpeg");
        } catch (Exception e) {
            LOG.error("Failed to generate shared file", e.getMessage());
        } finally {
            if (bitmap != null) {
                bitmap.recycle();
            }
        }
    }

    private void shareAsHtml() {
        String fileDir = getFilesDir().getPath() + File.separator + REPORT_FOLDER;
        FileUtils.createOrExistsDir(fileDir);
        FileUtils.deleteFilesInDir(fileDir);
        String filepath = fileDir + File.separator + System.currentTimeMillis() + ".html";
        FileIOUtils.writeFileFromString(new File(filepath), this.html);
        share(this, filepath, "text/html; charset=utf-8");
    }


    private void shareAsPDF() {
        showLoadingDialog();

        String fileDir =  getFilesDir().getPath() + File.separator + REPORT_FOLDER;
        FileUtils.createOrExistsDir(fileDir);
        FileUtils.deleteFilesInDir(fileDir);


        String jobName = getString(R.string.app_name) + " Document";
        PrintAttributes attributes = new PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
            .setResolution(new PrintAttributes.Resolution("pdf", "pdf", 600, 600))
            .setMinMargins(PrintAttributes.Margins.NO_MARGINS).build();
        String filepath = fileDir + File.separator + System.currentTimeMillis() + ".pdf";
        final PrintDocumentAdapter printAdapter = browser.createPrintDocumentAdapter(jobName);
        new ToPdfTask(attributes, printAdapter, filepath).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public class ToPdfTask extends  PrintPdfTask {

        public ToPdfTask(PrintAttributes printAttributes, PrintDocumentAdapter adapter, String printTo) {
            super(printAttributes, adapter, printTo);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            share(DisplayReportActivity.this, this.printTo, "application/pdf");
            dismissLoadingDialog();
        }
    }

    @NonNull
    private Bitmap getScreenShootBitmap() {
        float scale = browser.getScale();
        int width = browser.getWidth();
        int height = (int) (browser.getContentHeight() * scale + 0.5);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        browser.draw(canvas);
        return bitmap;
    }

    @OnClick(R.id.title_bar_actions)
    public void onShareClicked() {
        initShareFileTypeSheet();
        bottomSheetLayout.showWithSheetView(selectShareFileTypeSheet);
    }

    private void initShareFileTypeSheet() {
        if (selectShareFileTypeSheet == null) {
            selectShareFileTypeSheet =
                new MenuSheetView(this, MenuSheetView.MenuType.LIST, R.string.report_label_share_file_type,
                    new MenuSheetView.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            item.setChecked(true);
                            switch (item.getItemId()) {
                                case R.id.image_type:
                                    shareAsImage();
                                    break;
                                case R.id.pdf_type:
                                    shareAsPDF();
                                    break;
                            }
                            if (bottomSheetLayout.isSheetShowing()) {
                                bottomSheetLayout.dismissSheet();
                            }
                            return true;
                        }
                    });
            selectShareFileTypeSheet.inflateMenu(R.menu.report_type);
            selectShareFileTypeSheet.setListItemLayoutRes(R.layout.sheet_list_no_icon_item);
        }
    }


    public void share(Context context, String filePath, String type) {
        try {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.setType(type);
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, context.getResources().getString(R.string.title_display_report));
            sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            Uri attachmentUri = FileProvider.getUriForFile(context, context.getPackageName(), new File(filePath));
            sendIntent.putExtra(Intent.EXTRA_STREAM, attachmentUri);

            ActivityUtils.startActivity(Intent.createChooser(sendIntent, context.getResources().getString(R.string.display_report_share_title)));
        } catch (java.lang.Throwable ex) {
            LOG.error("Failed to generate report share file", ex);
            showToast(R.string.display_report_error_failed_create_file, Toast.LENGTH_SHORT, Gravity.CENTER);
        }
    }

}
