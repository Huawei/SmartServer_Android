package com.huawei.smart.server.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.huawei.smart.server.BaseActivity;
import com.huawei.smart.server.HWApplication;
import com.huawei.smart.server.R;
import com.huawei.smart.server.adapter.ImagePickerAdapter;
import com.huawei.smart.server.dialog.LoadingDialog;
import com.huawei.smart.server.utils.FilenameUtils;
import com.huawei.smart.server.validator.EmailValidator;
import com.huawei.smart.server.widget.GlideImageLoader;
import com.huawei.smart.server.widget.LabeledEditTextView;
import com.huawei.smart.server.widget.LabeledSwitch;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.bean.ImageItem;
import com.lzy.imagepicker.ui.ImageGridActivity;
import com.lzy.imagepicker.ui.ImagePreviewDelActivity;
import com.lzy.imagepicker.view.CropImageView;

import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 反馈建议
 */
public class SubmitFeedbackActivity extends BaseActivity implements ImagePickerAdapter.OnRecyclerViewItemClickListener {

    public static final int IMAGE_ITEM_ADD = -1;
    public static final int REQUEST_CODE_SELECT = 100;
    public static final int REQUEST_CODE_PREVIEW = 101;
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SubmitFeedbackActivity.class.getSimpleName());

    @BindView(R.id.name) LabeledEditTextView name;
    @BindView(R.id.email) LabeledEditTextView email;
    @BindView(R.id.content) EditText content;
    @BindView(R.id.screenshots) RecyclerView screenshotRecyclerView;
    @BindView(R.id.allow_upload_log) LabeledSwitch allowUploadLog;
    ArrayList<ImageItem> images = null;
    private ArrayList<ImageItem> screenshots;
    private ImagePickerAdapter adapter;
    private int maxImgCount = 2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_feedback);
        this.initialize(R.string.title_submit_feedback, true);
        name.getText().setFilters(new InputFilter[]{new InputFilter.LengthFilter(48)});
        email.getText().setFilters(new InputFilter[]{new InputFilter.LengthFilter(48)});
        initializeImagePicker();
        initUploadScreenshotView();
    }

    @Override
    protected boolean fitsSystemWindows() {
        return true;
    }

    private void initializeImagePicker() {
        ImagePicker imagePicker = ImagePicker.getInstance();
        imagePicker.setImageLoader(new GlideImageLoader());   //设置图片加载器
        imagePicker.setShowCamera(false);                      //显示拍照按钮
        imagePicker.setCrop(true);                           //允许裁剪（单选才有效）
        imagePicker.setSaveRectangle(true);                   //是否按矩形区域保存
        imagePicker.setSelectLimit(maxImgCount);              //选中数量限制
        imagePicker.setStyle(CropImageView.Style.RECTANGLE);  //裁剪框的形状
        imagePicker.setFocusWidth(800);                       //裁剪框的宽度。单位像素（圆形自动取宽高最小值）
        imagePicker.setFocusHeight(800);                      //裁剪框的高度。单位像素（圆形自动取宽高最小值）
        imagePicker.setOutPutX(1000);                         //保存文件的宽度。单位像素
        imagePicker.setOutPutY(1000);                         //保存文件的高度。单位像素
    }

    private void initUploadScreenshotView() {
        screenshots = new ArrayList<>();

        adapter = new ImagePickerAdapter(this, screenshots, maxImgCount);
        adapter.setOnItemClickListener(this);

        screenshotRecyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        screenshotRecyclerView.setHasFixedSize(true);
        screenshotRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(View view, int position) {
        switch (position) {
            case IMAGE_ITEM_ADD:
                ImagePicker.getInstance().setSelectLimit(maxImgCount - screenshots.size());
                Intent intent1 = new Intent(SubmitFeedbackActivity.this, ImageGridActivity.class);
                                /* 如果需要进入选择的时候显示已经选中的图片，
                                 * 详情请查看ImagePickerActivity
                                 *
                intent1.putExtra(ImageGridActivity.EXTRAS_IMAGES, screenshots);
                */
                startActivityForResult(intent1, REQUEST_CODE_SELECT);
                break;
            default:
                //打开预览
                Intent intentPreview = new Intent(this, ImagePreviewDelActivity.class);
                intentPreview.putExtra(ImagePicker.EXTRA_IMAGE_ITEMS, (ArrayList<ImageItem>) adapter.getImages());
                intentPreview.putExtra(ImagePicker.EXTRA_SELECTED_IMAGE_POSITION, position);
                intentPreview.putExtra(ImagePicker.EXTRA_FROM_ITEMS, true);
                startActivityForResult(intentPreview, REQUEST_CODE_PREVIEW);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {
            //添加图片返回
            if (data != null && requestCode == REQUEST_CODE_SELECT) {
                images = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
                if (images != null) {
                    screenshots.addAll(images);
                    adapter.setImages(screenshots);
                }
            }
        } else if (resultCode == ImagePicker.RESULT_CODE_BACK) {
            //预览图片返回
            if (data != null && requestCode == REQUEST_CODE_PREVIEW) {
                images = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_IMAGE_ITEMS);
                if (images != null) {
                    screenshots.clear();
                    screenshots.addAll(images);
                    adapter.setImages(screenshots);
                }
            }
        }
    }


    @OnClick(R.id.submit)
    public void onSubmit() {
        final String name = this.name.getText().toString();
        if (TextUtils.isEmpty(name)) {
            this.showToast(R.string.feedback_v_name_is_required, Toast.LENGTH_SHORT, Gravity.CENTER);
            return;
        } else if (name.length() > 48) {
            this.showToast(R.string.feedback_v_name_too_long, Toast.LENGTH_SHORT, Gravity.CENTER);
            return;
        }

        final String email = this.email.getText().toString();
        if (TextUtils.isEmpty(email) || !EmailValidator.getInstance().isValid(email)) {
            this.showToast(R.string.feedback_v_email_is_required, Toast.LENGTH_SHORT, Gravity.CENTER);
            return;
        } else if (email.length() > 48) {
            this.showToast(R.string.feedback_v_email_too_long, Toast.LENGTH_SHORT, Gravity.CENTER);
            return;
        }

        final String content = this.content.getText().toString();
        if (TextUtils.isEmpty(content)) {
            this.showToast(R.string.feedback_v_content_is_required, Toast.LENGTH_SHORT, Gravity.CENTER);
            return;
        } else if (content.length() > 256) {
            this.showToast(R.string.feedback_v_content_too_long, Toast.LENGTH_SHORT, Gravity.CENTER);
            return;
        }

        final boolean withLog = this.allowUploadLog.isChecked();

        sendMail(name, email, content, withLog, screenshots);
    }


    public void sendMail(final String name, final String email, final String content, boolean withLog, ArrayList<ImageItem> screenshots) {
        final LoadingDialog loadingDialog = showLoadingDialog();
        new SendMailTask(name, email, content, withLog, screenshots, loadingDialog).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    class SendMailTask extends AsyncTask<Void, Void, Boolean> {

        private final LoadingDialog loadingDialog;
        private final String name;
        private final String email;
        private final String content;
        private final boolean withLog;
        private final ArrayList<ImageItem> screenshots;

        public SendMailTask(final String name, final String email, final String content,
                            boolean withLog, ArrayList<ImageItem> screenshots, LoadingDialog loadingDialog) {
            this.loadingDialog = loadingDialog;
            this.name = name;
            this.email = email;
            this.content = content;
            this.withLog = withLog;
            this.screenshots = screenshots;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            LOG.info("Start new feedback");

            final String username = "woocupid@163.com";
            final String password = "1QaZ2WsX";

            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.debug", "false");
            props.put("mail.smtp.starttls.enable", "false");
            props.put("mail.smtp.host", "smtp.163.com");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.port", "994");
            props.put("mail.smtp.port", "994");

            Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });
            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress("woocupid@163.com"));
                message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse("smartserver@huawei.com"));
                message.setSubject("SmartServer 用户反馈");

                Multipart multipart = new MimeMultipart();

                // content
                MimeBodyPart contentBodyPart = new MimeBodyPart();
                contentBodyPart.setText(String.format("用户名称：%s \n用户邮箱：%s\n反馈内容：%s\n", name, email, content));
                multipart.addBodyPart(contentBodyPart);

                if (withLog) {
                    String filepath = HWApplication.getLogFileFolder() + File.separator + "smart-server.0.0.log";
                    multipart.addBodyPart(addAttachment("SmartServer.log.txt", filepath));  // attachment log
                }

                if (screenshots != null && screenshots.size() > 0) {
                    for (int idx = 0; idx < screenshots.size(); idx++) {
                        final ImageItem screenshot = screenshots.get(idx);
                        final String ext = FilenameUtils.getExtension(screenshot.name);
                        multipart.addBodyPart(addAttachment("Screenshot" + (idx + 1) + "." + ext, screenshot.path));  // attachment log
                    }
                }

                message.setContent(multipart);
                Transport.send(message);
                return true;
            } catch (MessagingException e) {
                LOG.error("Failed to send feedback mail", e);
                return false;
            } catch (Exception e) {
                LOG.error("Failed to send feedback mail", e);
                return false;
            }
        }

        private MimeBodyPart addAttachment(String fileName, String filepath) throws MessagingException {
            MimeBodyPart attach = new MimeBodyPart();
            DataSource source = new FileDataSource(filepath);
            attach.setDataHandler(new DataHandler(source));
            attach.setFileName(fileName);
            return attach;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (loadingDialog != null && loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }

            if (result) {
                SubmitFeedbackActivity.this.showToast(R.string.msg_action_success, Toast.LENGTH_SHORT, Gravity.CENTER);
                LOG.info("Feedback send successfully");
                finish();
            } else {
                SubmitFeedbackActivity.this.showToast(R.string.msg_access_api_failed, Toast.LENGTH_SHORT, Gravity.CENTER);
            }
        }
    }
}
