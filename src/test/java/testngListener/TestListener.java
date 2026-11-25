package testngListener;

import java.nio.file.Path;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

import config.ConfigManager;
import utils.FileUtils;
import utils.ScreenshotUtils;

/**
 * Logs test progress to the console, writes an Extent HTML report and attaches
 * a screenshot to every failed UI test.
 */
public class TestListener implements ITestListener {

    private static ExtentReports extent;
    private static final ThreadLocal<ExtentTest> CURRENT_TEST = new ThreadLocal<>();

    private static synchronized ExtentReports getExtent() {
        if (extent == null) {
            String reportDir = ConfigManager.getInstance().get("report.path", "reports");
            FileUtils.ensureDirectoryExists(reportDir);
            ExtentSparkReporter spark = new ExtentSparkReporter(Path.of(reportDir, "ExtentReport.html").toString());
            spark.config().setDocumentTitle("Automation Report");
            spark.config().setReportName("Twitch UI & IPStack API");
            extent = new ExtentReports();
            extent.attachReporter(spark);
            extent.setSystemInfo("Browser", ConfigManager.getInstance().getBrowser());
            extent.setSystemInfo("Device", ConfigManager.getInstance().getDeviceName());
        }
        return extent;
    }

    @Override
    public void onTestStart(ITestResult result) {
        System.out.println("STARTING TEST : " + result.getName());
        String description = result.getMethod().getDescription();
        ExtentTest test = getExtent().createTest(result.getName(), description);
        test.assignCategory(result.getTestContext().getName());
        CURRENT_TEST.set(test);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        System.out.println("PASSED TEST : " + result.getName());
        CURRENT_TEST.get().pass("Test passed");
        CURRENT_TEST.remove();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        System.out.println("FAILED TEST : " + result.getName());
        ExtentTest test = CURRENT_TEST.get();
        test.fail(result.getThrowable());
        String screenshot = ScreenshotUtils.capture(result.getName());
        if (screenshot != null) {
            System.out.println("Screenshot saved : " + screenshot);
            test.addScreenCaptureFromPath(screenshot);
        }
        CURRENT_TEST.remove();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        System.out.println("SKIPPED TEST : " + result.getName());
        ExtentTest test = CURRENT_TEST.get();
        if (test == null) {
            test = getExtent().createTest(result.getName());
        }
        test.skip(result.getThrowable() == null ? "Skipped" : result.getThrowable().toString());
        CURRENT_TEST.remove();
    }

    @Override
    public void onFinish(ITestContext context) {
        getExtent().flush();
        System.out.println("TEST EXECUTION FINISHED : " + context.getName());
    }
}
