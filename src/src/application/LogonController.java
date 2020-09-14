package application;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JFileChooser;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.UploadPartRequest;
import com.amazonaws.services.s3.model.UploadPartResult;
import com.amazonaws.services.s3.transfer.MultipleFileUpload;
import com.amazonaws.services.s3.transfer.Transfer;
import com.amazonaws.services.s3.transfer.Transfer.TransferState;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.TransferProgress;
import com.amazonaws.services.s3.transfer.Upload;
import com.amazonaws.util.StringUtils;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class LogonController implements Initializable {


	@FXML   private TextField accessKeyField;
	@FXML   private TextField secretAccessKeyField;
	@FXML   private TextField regionField;
	@FXML	private Button logonOk;
	@FXML	private Button logonCancel;
	@FXML	private TextArea msgArea;

	private  AmazonS3 s3client;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		initalizeCols();
	}

	private void initalizeCols() {

		accessKeyField.setText("AKIAJA2QJHIIAXZ6R4XQ");
		secretAccessKeyField.setText("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");
		regionField.setText(Regions.US_EAST_1.toString());

	}

	public AmazonS3 getS3Client()
	{
		return s3client;
	}

	public void logonOk()
	{

		String accessKey = accessKeyField.getText();
		String secretAccessKey = secretAccessKeyField.getText();
		String region = regionField.getText();

		try {
		AWSCredentials credentials = new BasicAWSCredentials(accessKey,
				secretAccessKey);
		s3client = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials))
				.withRegion(Regions.US_EAST_1).build();

		List<Bucket> bucketList = s3client.listBuckets();
		}
		catch (Exception e)
		{
			msgArea.setText(e.toString());
			return;
		}
		  // get a handle to the stage
		  Stage stage = (Stage) logonOk.getScene().getWindow();
		  // do what you have to do
		  stage.close();
	}
	public void logonCancel()
	{
		System.out.println("test");
		  // get a handle to the stage
		  Stage stage = (Stage) logonCancel.getScene().getWindow();
		  // do what you have to do
		  s3client = null;
		  stage.close();

	}

}
