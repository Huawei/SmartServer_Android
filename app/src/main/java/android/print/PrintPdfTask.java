package android.print;

import android.os.AsyncTask;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;

import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.CountDownLatch;

/**
 * @author coa.ke on 3/23/18
 */
public abstract class PrintPdfTask extends AsyncTask<Void, String, Void> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(PrintPdfTask.class.getSimpleName());

    private static final String TAG = PrintPdfTask.class.getSimpleName();
    protected final PrintAttributes printAttributes;
    protected final PrintDocumentAdapter adapter;
    protected final String printTo;

    public PrintPdfTask(PrintAttributes printAttributes, PrintDocumentAdapter adapter, String printTo) {
        this.printAttributes = printAttributes;
        this.adapter = adapter;
        this.printTo = printTo;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        final CountDownLatch latch = new CountDownLatch(1);
        adapter.onLayout(null, printAttributes, null,
            new PrintDocumentAdapter.LayoutResultCallback() {
                @Override
                public void onLayoutFinished(PrintDocumentInfo info, boolean changed) {
                    adapter.onWrite(
                        new PageRange[]{
                            PageRange.ALL_PAGES
                        },
                        getOutputFile(printTo),
                        new CancellationSignal(),
                        new PrintDocumentAdapter.WriteResultCallback() {
                            @Override
                            public void onWriteFinished(PageRange[] pages) {
                                super.onWriteFinished(pages);
                                latch.countDown();
                            }
                        });
                }
            }, null);

        try {
            latch.await();
        } catch (InterruptedException e) {

        }
        return null;
    }

    private ParcelFileDescriptor getOutputFile(String filepath) {
        try {
            final File file = new File(filepath);
            if (!file.exists()) {
                file.createNewFile();
            }
            return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_WRITE);
        } catch (Exception e) {
            LOG.error("Failed to open ParcelFileDescriptor", e);
        }
        return null;
    }

}