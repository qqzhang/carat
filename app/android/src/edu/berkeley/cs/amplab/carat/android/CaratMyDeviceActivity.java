package edu.berkeley.cs.amplab.carat.android;

import java.util.ArrayList;
import java.util.List;

import edu.berkeley.cs.amplab.carat.android.lists.ProcessInfoAdapter;
import edu.berkeley.cs.amplab.carat.android.sampling.SamplingLibrary;
import edu.berkeley.cs.amplab.carat.android.storage.CaratDataStorage;
import edu.berkeley.cs.amplab.carat.android.ui.BaseVFActivity;
import edu.berkeley.cs.amplab.carat.android.ui.DrawView;
import edu.berkeley.cs.amplab.carat.android.ui.FlipperBackListener;
import edu.berkeley.cs.amplab.carat.android.ui.SwipeListener;
import edu.berkeley.cs.amplab.carat.android.ui.UiRefreshThread;
import edu.berkeley.cs.amplab.carat.android.CaratApplication.Type;
import edu.berkeley.cs.amplab.carat.thrift.DetailScreenReport;
import edu.berkeley.cs.amplab.carat.thrift.ProcessInfo;
import edu.berkeley.cs.amplab.carat.thrift.Reports;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ProgressBar;
import android.widget.ViewFlipper;

/**
 * 
 * @author Eemil Lagerspetz
 * 
 */
public class CaratMyDeviceActivity extends BaseVFActivity {

    private DrawView osView = null;
    private View osViewPage = null;
    private DrawView modelView = null;
    private View modelViewPage = null;
    private DrawView appsView = null;
    private View appsViewPage = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mydevice);

        vf = (ViewFlipper) findViewById(R.id.viewFlipper);
        View baseView = findViewById(R.id.scrollView1);
        baseView.setOnTouchListener(SwipeListener.instance);
        baseViewIndex = vf.indexOfChild(baseView);
        initJscoreView();
        initMemoryView();
        initProcessListView();
        initOsView();
        initModelView();
        initAppsView();
        setModelAndVersion();

        Object o = getLastNonConfigurationInstance();
        if (o != null) {
            CaratMyDeviceActivity previous = (CaratMyDeviceActivity) o;
            List<DrawView> views = new ArrayList<DrawView>();
            views.add(previous.osView);
            views.add(previous.modelView);
            views.add(previous.appsView);
            for (DrawView v : views) {
                double[] xVals = v.getXVals();
                double[] yVals = v.getYVals();
                Type t = v.getType();
                double[] xValsWithout = v.getXValsWithout();
                double[] yValsWithout = v.getYValsWithout();
                String appName = v.getAppName();
                if (v == previous.osView) {
                    osView.setParams(t, appName, xVals, yVals, xValsWithout,
                            yValsWithout);
                    osView.postInvalidate();
                } else if (v == previous.modelView) {
                    modelView.setParams(t, appName, xVals, yVals, xValsWithout,
                            yValsWithout);
                    modelView.postInvalidate();
                } else if (v == previous.appsView) {
                    appsView.setParams(t, appName, xVals, yVals, xValsWithout,
                            yValsWithout);
                    appsView.postInvalidate();
                }
            }

            restorePage(osViewPage, previous.osViewPage);
            restorePage(modelViewPage, previous.modelViewPage);
            restorePage(appsViewPage, previous.appsViewPage);
        }

        if (viewIndex == 0)
            vf.setDisplayedChild(baseViewIndex);
        else
            vf.setDisplayedChild(viewIndex);
    }
    
    private void restorePage(View thisPage, View oldPage){
        TextView pn = (TextView) oldPage.findViewById(R.id.name);
        ImageView pi = (ImageView) oldPage.findViewById(R.id.appIcon);
        ProgressBar pp = (ProgressBar) oldPage.findViewById(R.id.confidenceBar);
        
        ((TextView) thisPage.findViewById(R.id.name)).setText(pn.getText());
        ((ImageView) thisPage.findViewById(R.id.appIcon))
                .setImageDrawable(pi.getDrawable());
        ((ProgressBar) thisPage.findViewById(R.id.confidenceBar))
                .setProgress(pp.getProgress());
    }

    private void initJscoreView() {
        WebView webview = (WebView) findViewById(R.id.jscoreView);
        // Fixes the white flash when showing the page for the first time.
        if (getString(R.string.blackBackground).equals("true"))
            webview.setBackgroundColor(0);
        /*
         * 
         * 
         * webview.getSettings().setJavaScriptEnabled(true);
         */
        /*
         * To display the amplab_logo, we need to have it stored in assets as
         * well. If we don't want to do that, the loadConvoluted method below
         * avoids it.
         */
        webview.loadUrl("file:///android_asset/jscoreinfo.html");
        webview.setOnTouchListener(new FlipperBackListener(this, vf, vf
                .indexOfChild(findViewById(R.id.scrollView1))));
    }

    private void initMemoryView() {
        WebView webview = (WebView) findViewById(R.id.memoryView);
        // Fixes the white flash when showing the page for the first time.
        if (getString(R.string.blackBackground).equals("true"))
            webview.setBackgroundColor(0);
        /*
         * 
         * 
         * webview.getSettings().setJavaScriptEnabled(true);
         */
        /*
         * To display the amplab_logo, we need to have it stored in assets as
         * well. If we don't want to do that, the loadConvoluted method below
         * avoids it.
         */
        webview.loadUrl("file:///android_asset/memoryinfo.html");
        webview.setOnTouchListener(new FlipperBackListener(this, vf, vf
                .indexOfChild(findViewById(R.id.scrollView1))));
    }

    private void initProcessListView() {
        final ListView lv = (ListView) findViewById(R.id.processList);
        lv.setCacheColorHint(0);
        // Ignore clicks here.
        /*
         * lv.setOnItemClickListener(new OnItemClickListener() {
         * 
         * @Override public void onItemClick(AdapterView<?> a, View v, int
         * position, long id) { Object o = lv.getItemAtPosition(position);
         * RunningAppProcessInfo fullObject = (RunningAppProcessInfo) o;
         * Toast.makeText(CaratMyDeviceActivity.this, "You have chosen: " + " "
         * + fullObject.processName, Toast.LENGTH_LONG).show(); } });
         */
        List<ProcessInfo> searchResults = SamplingLibrary
                .getRunningAppInfo(getApplicationContext());
        lv.setAdapter(new ProcessInfoAdapter(this, searchResults));
        lv.setOnTouchListener(new FlipperBackListener(this, vf, vf
                .indexOfChild(findViewById(R.id.scrollView1))));
    }

    private View[] construct() {
        View[] result = new View[2];
        LayoutInflater inflater = (LayoutInflater) getApplicationContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View detailPage = inflater.inflate(R.layout.graph, null);
        ViewGroup g = (ViewGroup) detailPage;
        DrawView w = new DrawView(getApplicationContext());
        g.addView(w);
        vf.addView(detailPage);

        g.setOnTouchListener(new FlipperBackListener(this, vf, baseViewIndex,
                true));
        result[0] = w;
        result[1] = detailPage;

        final WebView webview = new WebView(getApplicationContext());
        // Fixes the white flash when showing the page for the first time.
        if (getString(R.string.blackBackground).equals("true"))
            webview.setBackgroundColor(0);

        webview.loadUrl("file:///android_asset/detailinfo.html");
        webview.setOnTouchListener(new FlipperBackListener(this, vf, vf
                .indexOfChild(detailPage), false));
        vf.addView(webview);

        View moreinfo = detailPage.findViewById(R.id.moreinfo);
        moreinfo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                switchView(webview);
            }
        });

        return result;
    }

    private void initOsView() {
        View[] viewAndPage = construct();
        osView = (DrawView) viewAndPage[0];
        osViewPage = viewAndPage[1];
    }

    private void initModelView() {
        View[] viewAndPage = construct();
        modelView = (DrawView) viewAndPage[0];
        modelViewPage = viewAndPage[1];
    }

    private void initAppsView() {
        View[] viewAndPage = construct();
        appsView = (DrawView) viewAndPage[0];
        appsViewPage = viewAndPage[1];
    }

    /**
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
        CaratApplication.setMyDevice(this);
        UiRefreshThread.setReportData();
        new Thread() {
            public void run() {
                synchronized (UiRefreshThread.getInstance()) {
                    UiRefreshThread.getInstance().appResumed();
                }
            }
        }.start();

        setMemory();
        super.onResume();
    }

    /**
     * Called when OS additional info button is clicked.
     * 
     * @param v
     *            The source of the click.
     */
    public void showOsInfo(View v) {
        Reports r = CaratApplication.s.getReports();
        if (r != null) {
            DetailScreenReport os = r.getOs();
            DetailScreenReport osWithout = r.getOsWithout();

            String label = "OS: " + SamplingLibrary.getOsVersion();
            Drawable icon = CaratApplication.iconForApp(getApplicationContext(), "Carat");
            ((TextView) osViewPage.findViewById(R.id.name)).setText(label);
            ((ImageView) osViewPage.findViewById(R.id.appIcon))
                    .setImageDrawable(icon);
            Log.v("OsInfo", "Os score: " + os.getScore());
            ((ProgressBar) osViewPage.findViewById(R.id.confidenceBar))
                    .setProgress((int) (os.getScore() * 100));

            osView.setParams(Type.OS, SamplingLibrary.getOsVersion(),
                    CaratDataStorage.convert(os.getXVals()), CaratDataStorage.convert(os.getYVals()), CaratDataStorage.convert(osWithout.getXVals()),
                    CaratDataStorage.convert(osWithout.getYVals()));
        }
        switchView(osViewPage);
    }

    /**
     * Called when Device additional info button is clicked.
     * 
     * @param v
     *            The source of the click.
     */
    public void showDeviceInfo(View v) {
        Reports r = CaratApplication.s.getReports();
        if (r != null) {
            DetailScreenReport model = r.getModel();
            DetailScreenReport modelWithout = r.getModelWithout();

            String label = "Model: " + SamplingLibrary.getModel();
            Drawable icon = CaratApplication.iconForApp(getApplicationContext(), "Carat");
            ((TextView) modelViewPage.findViewById(R.id.name)).setText(label);
            ((ImageView) modelViewPage.findViewById(R.id.appIcon))
                    .setImageDrawable(icon);

            Log.v("ModelInfo", "Model score: " + model.getScore());
            ((ProgressBar) modelViewPage.findViewById(R.id.confidenceBar))
                    .setProgress((int) (model.getScore() * 100));

            modelView.setParams(Type.MODEL, SamplingLibrary.getModel(),
                    CaratDataStorage.convert(model.getXVals()), CaratDataStorage.convert(model.getYVals()),
                    CaratDataStorage.convert(modelWithout.getXVals()), CaratDataStorage.convert(modelWithout.getYVals()));
        }
        switchView(modelViewPage);
    }

    /**
     * Called when App list additional info button is clicked.
     * 
     * @param v
     *            The source of the click.
     */
    public void showAppInfo(View v) {
        Reports r = CaratApplication.s.getReports();
        if (r != null) {
            DetailScreenReport similar = r.getSimilarApps();
            DetailScreenReport similarWithout = r.getSimilarAppsWithout();

            String label = "Similar apps";
            Drawable icon = CaratApplication.iconForApp(getApplicationContext(), "Carat");
            ((TextView) appsViewPage.findViewById(R.id.name)).setText(label);
            ((ImageView) appsViewPage.findViewById(R.id.appIcon))
                    .setImageDrawable(icon);

            Log.v("SimilarInfo", "Similar score: " + similar.getScore());

            ((ProgressBar) appsViewPage.findViewById(R.id.confidenceBar))
                    .setProgress((int) (similar.getScore() * 100));

            appsView.setParams(Type.SIMILAR, SamplingLibrary.getModel(),
                    CaratDataStorage.convert(similar.getXVals()), CaratDataStorage.convert(similar.getYVals()),
                    CaratDataStorage.convert(similarWithout.getXVals()), CaratDataStorage.convert(similarWithout.getYVals()));
        }
        switchView(appsViewPage);
    }

    /**
     * Called when Memory additional info button is clicked.
     * 
     * @param v
     *            The source of the click.
     */
    public void showMemoryInfo(View v) {
        switchView(R.id.memoryView);
    }

    /**
     * Called when J-Score additional info button is clicked.
     * 
     * @param v
     *            The source of the click.
     */
    public void viewJscoreInfo(View v) {
        switchView(R.id.jscoreView);
    }

    /**
     * Called when View Process List is clicked.
     * 
     * @param v
     *            The source of the click.
     */
    public void viewProcessList(View v) {
        // prepare content:
        ListView lv = (ListView) findViewById(R.id.processList);
        List<ProcessInfo> searchResults = SamplingLibrary
                .getRunningAppInfo(getApplicationContext());
        lv.setAdapter(new ProcessInfoAdapter(this, searchResults));
        // switch views:
        switchView(R.id.processList);
    }

    private void setModelAndVersion() {
        // Device model
        String model = SamplingLibrary.getModel();

        // Android version
        String version = SamplingLibrary.getOsVersion();

        Window win = this.getWindow();
        // The info icon needs to change from dark to light.
        TextView mText = (TextView) win.findViewById(R.id.dev_value);
        mText.setText(model);
        mText = (TextView) win.findViewById(R.id.os_ver_value);
        mText.setText(version);
    }

    private void setMemory() {
        final Window win = this.getWindow();
        // Set memory values to the progress bar.
        ProgressBar mText = (ProgressBar) win.findViewById(R.id.progressBar1);
        int[] totalAndUsed = SamplingLibrary.readMeminfo();
        mText.setMax(totalAndUsed[0] + totalAndUsed[1]);
        mText.setProgress(totalAndUsed[0]);
        mText = (ProgressBar) win.findViewById(R.id.progressBar2);

        if (totalAndUsed.length > 2) {
            mText.setMax(totalAndUsed[2] + totalAndUsed[3]);
            mText.setProgress(totalAndUsed[2]);
        }

        runOnUiThread(new Runnable() {
            public void run() {
                final double cpu = SamplingLibrary.readUsage();
                /* CPU usage */
                ProgressBar mText = (ProgressBar) win.findViewById(R.id.cpubar);
                mText.setMax(100);
                mText.setProgress((int) (cpu * 100));
            }
        });
    }
}
