package application;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

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
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.BucketPolicy;
import com.amazonaws.services.s3.model.CanonicalGrantee;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.Grant;
import com.amazonaws.services.s3.model.GroupGrantee;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ListVersionsRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.S3VersionSummary;
import com.amazonaws.services.s3.model.UploadPartRequest;
import com.amazonaws.services.s3.model.UploadPartResult;
import com.amazonaws.services.s3.model.VersionListing;
import com.amazonaws.services.s3.transfer.MultipleFileUpload;
import com.amazonaws.services.s3.transfer.Transfer;
import com.amazonaws.services.s3.transfer.Transfer.TransferState;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.TransferProgress;
import com.amazonaws.services.s3.transfer.Upload;
import com.amazonaws.util.StringUtils;

import javafx.application.Platform;
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
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MainController implements Initializable {

	@FXML	private TableColumn<S3object, String> fileName;
	@FXML	private TableColumn<S3object, String> fileSize;
	@FXML	private TableColumn<S3object, String> fileType;
	@FXML	private TableColumn<S3object, String> fileLastModified;
	@FXML	private TableColumn<S3object, String> fileStorageClass;

	@FXML	public ListView buckets;
	@FXML	public TableView objects;
	@FXML	public Button upload;
	@FXML	public Button download;
	@FXML	public Button view;
	@FXML	public Button connect;
	@FXML	public Button prev;
	@FXML	public Button next;

	@FXML   public TextField maxKeys;

	// @FXML   public TextArea textArea;
	@FXML   public TextField folderField;

	// For the tabs

	@FXML	public Tab tabPermissions;
	@FXML	public Tab tabHttpHeaders;
	@FXML	public Tab tabTags;
	@FXML	public Tab tabProperties;
	@FXML	public Tab tabPreview;
	@FXML	public Tab tabVersions;


	@FXML	private TextArea bucketPolicyText;
	@FXML	private ListView objectPermissionsListView;
	@FXML	private ListView consoleListView;
	@FXML	private Label counterLabel;

	@FXML	private Button pauseButton;
	@FXML	private Button resumeButton;
	@FXML   private Button stopButton;
	@FXML	private ProgressBar progressBar;
	@FXML	private Label transferCounter;
	ObservableList<S3object> list = FXCollections.observableArrayList();
	ObservableList<String> bucketNames = FXCollections.observableArrayList();
	ObservableList<S3object> s3objectsList = FXCollections.observableArrayList();
	ObservableList<String> objectPermissions = FXCollections.observableArrayList();
	ObservableList<String> consoleList = FXCollections.observableArrayList();


	Thread thread;
	String threadControl;
	public AmazonS3 s3client;

	private boolean root = true;

	ListObjectsV2Result result;
	ListObjectsV2Request req;

	ArrayList<String> tokenList = new ArrayList();
	int tokenIndex = 0;



	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		initalizeCols();
	}

	private void initalizeCols() {
		fileName.setCellValueFactory(new PropertyValueFactory<>("fileName"));
		fileSize.setCellValueFactory(new PropertyValueFactory<>("fileSize"));
		fileType.setCellValueFactory(new PropertyValueFactory<>("fileType"));
		fileLastModified.setCellValueFactory(new PropertyValueFactory<>("fileLastModified"));
		fileStorageClass.setCellValueFactory(new PropertyValueFactory<>("fileStorageClass"));
	}

	public void connect(ActionEvent event) throws Exception {

		// Open logon window

		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(getClass().getResource("LogonWindow.fxml"));
		Parent logonViewParent = loader.load();

		LogonController logonController = loader.getController();

		Scene logonScene = new Scene(logonViewParent);
		Stage stage = new Stage();
		stage.setTitle("SK3 Browser");
		stage.setScene(logonScene);
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.showAndWait();

		s3client = logonController.getS3Client();

		if (s3client != null)
		{
			List<Bucket> bucketList = s3client.listBuckets();

			for (Bucket bucket : bucketList) {
				bucketNames.add(bucket.getName());
			}
			buckets.getItems().clear();
			buckets.setItems(bucketNames);
			buckets.getSelectionModel().select(0);
			refreshlist();
			load();
			connect.setDisable(true);
		}

	}

	public void logonOk()
	{

		AWSCredentials credentials = new BasicAWSCredentials("AKIAJA2QJHIIAXZ6R4XQ",
				"mmES/ofzPyI4u60qwKF3IgGj0TZmFftxTfa6ITg7");
		s3client = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials))
				.withRegion(Regions.US_EAST_1).build();
		List<Bucket> bucketList = s3client.listBuckets();

		for (Bucket bucket : bucketList) {
			bucketNames.add(bucket.getName());
		}
		buckets.setItems(bucketNames);

	}

	public void bucketListSelect(MouseEvent event) {

		refreshlist();
		load();


	}
	
	public void refreshlist()
	{
		String bucketName = (String) buckets.getSelectionModel().getSelectedItem();


		String prefix = "";
		String delimiter = "/";

		int _maxKeys = 100;
		if (!maxKeys.getText().trim().equals(""))
			_maxKeys = Integer.parseInt(maxKeys.getText());


		req = new ListObjectsV2Request().withBucketName(bucketName).withPrefix(prefix).withDelimiter(delimiter).withMaxKeys(_maxKeys);

		tokenList.add(null);
		tokenIndex = 0;
		folderField.setText("");

		populateBucketPolicy(bucketName);

	}

	private void populateBucketPolicy(String bucketName)
	{
		  try {
		      BucketPolicy bucket_policy = s3client.getBucketPolicy(bucketName);
		      bucketPolicyText.setText(bucket_policy.getPolicyText());

		  } catch (AmazonServiceException e) {
		      System.err.println(e.getErrorMessage());
		      System.exit(1);
		  }
	}

	private void displayObjectVersions(String bucketName, String objectName)
	{

			//  Versions
			List<String> versionIds = new ArrayList<>();
	        ListVersionsRequest listVersionsRequest = new ListVersionsRequest();

	        listVersionsRequest.setBucketName(bucketName);
	        VersionListing versionListing;

	        do {
	            versionListing = s3client.listVersions(listVersionsRequest);
	            for (S3VersionSummary objectsummary : versionListing.getVersionSummaries()) {
	                //System.out.println(objectsummary.getKey() + ": " + objectsummary.getVersionId());
	                if (objectsummary.getKey().equals(objectName)) {
	                    versionIds.add(objectsummary.getVersionId());
	                    objectsummary.getETag();
	                    objectsummary.getLastModified();

	                }
	            }
	            listVersionsRequest.setKeyMarker(versionListing.getNextKeyMarker());
	            listVersionsRequest.setVersionIdMarker(versionListing.getNextVersionIdMarker());
	        } while (versionListing.isTruncated());

	        for (String versionId : versionIds) {
	            GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName,objectName );
	            getObjectRequest.setVersionId(versionId);
	            S3Object s3Object = s3client.getObject(getObjectRequest);
	            StringWriter stringWriter = new StringWriter();
	            Charset charset = Charset.forName("UTF-8");
	            s3Object.getObjectContent();
	        }

	}
	public void PrevPage()
	{

		if (tokenIndex > 0)	tokenIndex--;
		if (tokenIndex==0)	prev.setDisable(true);
		next.setDisable(false);
		load();


	}
	public void NextPage()
	{
		// This means there are more objects as button is enabled

		if (tokenList.size()-1 >  tokenIndex) tokenIndex++;
		prev.setDisable(false);
		load();

	}

	public void load()
	{

		// Remove all records from the object list
		s3objectsList = FXCollections.observableArrayList();

		// Remove rows from tableview
		objects.getItems().clear();

		req.setContinuationToken(tokenList.get(tokenIndex));

		result = s3client.listObjectsV2(req);


		if (folderField.getText().endsWith("/"))
			s3objectsList.addAll(new S3object("..", "", "", "", ""));

		// Get all prefixes first
		List<String> prefixes = result.getCommonPrefixes();
		for (int i = 0; i < prefixes.size(); i++) {
			String prefix = prefixes.get(i);
			if (folderField.getText().length() > 0)
				prefix = prefix.substring(folderField.getText().length());

			s3objectsList.addAll(new S3object(prefix, "", "Folder", "", ""));
		}

		// Get all objects
		for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
			// System.out.printf(" - %s (size: %d)\n", objectSummary.getKey(), objectSummary.getSize());

			String fileName = objectSummary.getKey();
			if (folderField.getText().length() > 0)
				fileName = fileName.substring(folderField.getText().length());
			String size = new Long(objectSummary.getSize()).toString();
			if (Integer.parseInt(size) > 0)
			{

				String lastModified = objectSummary.getLastModified().toString();
				String strorageClass = objectSummary.getStorageClass();
				s3objectsList.addAll(new S3object(fileName, size, "", lastModified, strorageClass));
			}


		}

		// If there are more than maxKeys keys in the bucket, get a
		// continuation token

		// and list the next objects.

		String currentToken = result.getNextContinuationToken();

		if (currentToken != null)
		{
			if (tokenList.size()-1 >  tokenIndex)
				tokenList.set(tokenIndex+1, currentToken);
			else
				tokenList.add(currentToken);
			next.setDisable(false);
		}
		else
			next.setDisable(true);
		objects.getItems().addAll(s3objectsList);
	}

	public void listFolder(MouseEvent event)
	{
		S3object folder = (S3object) objects.getSelectionModel().getSelectedItem();
		String bucketName = (String) buckets.getSelectionModel().getSelectedItem();
		if (folder == null)
			return;

		//System.out.println("Folder " + folder);
		String folderName = folder.getFileName();
		//System.out.println("Folder Name " + folderName);
		String currFolder = folderField.getText();

		if (event.getClickCount() == 2)
		{

			if (folder.getFileName().endsWith("/"))
			{


			folderField.setText(currFolder+folderName);
			processFolder();
			}
			if (folderName.equals(".."))
			{
				// Get the folder feild and remove the last folder
				String newFolder = folderField.getText();
				String[] build = newFolder.split("/");

				if (build.length == 1)
					folderField.setText("");
				else
				{
					// Reconstruct the field
					newFolder = build[0]+"/";
					for (int i=1; i < build.length-1;i++)
					{
						newFolder = newFolder+build[i]+"/";
					}
					folderField.setText(newFolder);
				}

				processFolder();


			}
			return;
		}
		if (event.getClickCount() == 1)
		{
			//String bucketName = (String) buckets.getSelectionModel().getSelectedItem();

			// ObjectMetadata objectMetadata = s3client.getObjectMetadata(bucketName,currFolder+folderName);
			displayObjectPermissions();
			// displayObjectVersions(bucketName,currFolder+folderName);
		}
	}

	public void displayObjectPermissions()
	{
		try {
			objectPermissionsListView.getItems().clear();
			S3object folder = (S3object) objects.getSelectionModel().getSelectedItem();
			String folderName = folder.getFileName();
			if (folderName.endsWith("/") || folderName.equals(".."))
				return;
			String currFolder = folderField.getText();
			String bucketName = (String) buckets.getSelectionModel().getSelectedItem();

		    AccessControlList acl = s3client.getObjectAcl(bucketName, currFolder+folderName);
		    List<Grant> grants = acl.getGrantsAsList();
		    for (Grant grant : grants) {
		    	if (grant.getGrantee() instanceof CanonicalGrantee)
	 				objectPermissions.add(((CanonicalGrantee) grant.getGrantee()).getDisplayName() +" : "+grant.getPermission().toString());
  				if (grant.getGrantee() instanceof GroupGrantee)
  					objectPermissions.add(grant.getGrantee().getIdentifier() + " : "+grant.getPermission().toString());

		    }
		    objectPermissionsListView.setItems(objectPermissions);
		} catch (AmazonServiceException e) {
		    System.err.println(e.getErrorMessage());
		    System.exit(1);
		}

	}
	public void processFolder()
	{
		//System.out.println(folderField.getText());
		String folderName = folderField.getText();
		req.withPrefix(folderName);
		resetList();
		load();

	}
	public void resetList()
	{
		tokenList = new ArrayList();
		tokenList.add(null);
		tokenIndex = 0;
		next.setDisable(true);
		prev.setDisable(true);
	}
	public void multiPartUploadx()
	{
		final JFileChooser fc = new JFileChooser();

	    FileChooser chooser = new FileChooser();
	    chooser.setTitle("Open File");
	    File file = chooser.showOpenDialog(new Stage());
		String bucketName = (String) buckets.getSelectionModel().getSelectedItem();
		uploadFileWithListener(file.getPath(), bucketName, folderField.getText()+file.getName(), true);
	}

	public void handleDragOver(DragEvent event)
	{
		if (event.getDragboard().hasFiles())
		{
			event.acceptTransferModes(TransferMode.COPY);
		}
	}

	public void handleDrop(DragEvent event)
	{
		Hashtable<String,String> fileHash = new Hashtable();
		consoleListView.getItems().clear();
		consoleListView.refresh();
		List<File> files = event.getDragboard().getFiles();
		// Process the file/files
		files.forEach(file -> {
				String basePath = file.getParent()+"\\";
				if (file.isDirectory())
				{
					String rootPath = file.getAbsolutePath();
					try {
						Files.walk(Paths.get(rootPath))
						.filter(Files::isRegularFile)
						.forEach(filePath -> {
							String absPath = filePath.toAbsolutePath().toString();
							fileHash.put(filePath.toAbsolutePath().toString(), basePath);
						}

						);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if (file.isFile())
					fileHash.put(file.getAbsolutePath(),basePath);

			}
		);

		thread = new Thread(new Runnable() {
			public void run()
			{

				fileHash.forEach((filePath, basePath) -> {
					File file = new File((String) filePath);

					Runnable updater = new Runnable() {
	                    @Override
	                    public void run() {
	                		consoleListView.getItems().add(file.getAbsolutePath());
	                		consoleListView.scrollTo(consoleListView.getItems().size());
	                		//consoleListView.refresh();
	                    }
	                };
	                Platform.runLater(updater);
					multiPartUpload(basePath,file);

					// The one with progress is causing exception of xml malformed.. need to check later.
					//String bucketName = (String) buckets.getSelectionModel().getSelectedItem();
					// uploadWithProgress(file.getPath(), bucketName, "");
		        });
				fileHash.clear();
			}
		});

		pauseButton.setDisable(false);
		stopButton.setDisable(false);
		resumeButton.setDisable(true);

		thread.start();

		consoleListView.scrollTo(consoleListView.getItems().size());
		pauseButton.setDisable(true);
		stopButton.setDisable(true);
		resumeButton.setDisable(true);

		load();

	}
	public void stopUpload()
	{
		pauseButton.setDisable(true);
		stopButton.setDisable(false);
		resumeButton.setDisable(true);
	}
	public void pauseUpload()
	{
		pauseButton.setDisable(true);
		stopButton.setDisable(false);
		resumeButton.setDisable(false);
	}
	public void resumeUpload()
	{
		pauseButton.setDisable(false);
		stopButton.setDisable(false);
		resumeButton.setDisable(false);
	}

	public void fileChooser()
	{
		final JFileChooser fc = new JFileChooser();
	    FileChooser chooser = new FileChooser();
	    chooser.setTitle("Open File");
	    File file = chooser.showOpenDialog(new Stage());

	}
	public void multiPartUpload(String basePath, File file)
	{

		//consoleListView.getItems().add(file.getAbsolutePath());
		//consoleListView.refresh();
        String filePath = "";

        long contentLength = file.length();
        long partSize = 5 * 1024 * 1024; // Set part size to 5 MB.
		String bucketName = (String) buckets.getSelectionModel().getSelectedItem();

        try {

            // Create a list of ETag objects. You retrieve ETags for each object part uploaded,
            // then, after each individual part has been uploaded, pass the list of ETags to
            // the request to complete the upload.
            List<PartETag> partETags = new ArrayList<PartETag>();

            // Initiate the multipart upload.
            String keyName=folderField.getText()+file.getName();

            int index = file.getAbsolutePath().indexOf(basePath) + basePath.length();

            keyName = file.getAbsolutePath().substring(index);

            keyName = folderField.getText() + keyName;

            keyName = toUrlPath(keyName);


            InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(bucketName, keyName);
            InitiateMultipartUploadResult initResponse = s3client.initiateMultipartUpload(initRequest);

            // Upload the file parts.
            long filePosition = 0;
            double progressStep = 1.0;
            if ( contentLength > partSize)
            	progressStep = (contentLength / partSize) / 100.00;
            double progressMeter = 0.00;
            progressBar.setProgress(0.10);

            for (int i = 1; filePosition < contentLength; i++) {
                // Because the last part could be less than 5 MB, adjust the part size as needed.
                partSize = Math.min(partSize, (contentLength - filePosition));

                // Create the request to upload a part.
                UploadPartRequest uploadRequest = new UploadPartRequest()
                        .withBucketName(bucketName)
                        .withKey(keyName)
                        .withUploadId(initResponse.getUploadId())
                        .withPartNumber(i)
                        .withFileOffset(filePosition)
                        .withFile(file)
                        .withPartSize(partSize);

                // Upload the part and add the response's ETag to our list.
                UploadPartResult uploadResult = s3client.uploadPart(uploadRequest);
                partETags.add(uploadResult.getPartETag());

                filePosition += partSize;
                progressMeter = progressMeter + progressStep;
                progressBar.setProgress(progressMeter);
            }
            progressBar.setProgress(0.00);
            // Complete the multipart upload.
            CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest(bucketName, keyName,
                    initResponse.getUploadId(), partETags);
            s3client.completeMultipartUpload(compRequest);
        } catch (AmazonServiceException e) {
            // The call was transmitted successfully, but Amazon S3 couldn't process
            // it, so it returned an error response.
            e.printStackTrace();
        } catch (SdkClientException e) {
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            e.printStackTrace();
        }
	}
	public static String toUrlPath(String path) {
		  return path.indexOf('\\') < 0 ? path : path.replace('\\', '/');
	}

    // waits for the transfer to complete, catching any exceptions that occur.
    public static void waitForCompletion(Transfer xfer) {
        try {
            xfer.waitForCompletion();
        } catch (AmazonServiceException e) {
            System.err.println("Amazon service error: " + e.getMessage());
            System.exit(1);
        } catch (AmazonClientException e) {
            System.err.println("Amazon client error: " + e.getMessage());
            System.exit(1);
        } catch (InterruptedException e) {
            System.err.println("Transfer interrupted: " + e.getMessage());
            System.exit(1);
        }
    }

    // Prints progress while waiting for the transfer to finish.
    public static void showTransferProgress(Transfer xfer) {
        // print the transfer's human-readable description
        //System.out.println(xfer.getDescription());
        // print an empty progress bar...
        printProgressBar(0.0);
        // update the progress bar while the xfer is ongoing.
        do {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                return;
            }
            // Note: so_far and total aren't used, they're just for
            // documentation purposes.
            TransferProgress progress = xfer.getProgress();
            long so_far = progress.getBytesTransferred();
            long total = progress.getTotalBytesToTransfer();
            double pct = progress.getPercentTransferred();
            eraseProgressBar();
            printProgressBar(pct);
        } while (xfer.isDone() == false);
        // print the final state of the transfer.
        TransferState xfer_state = xfer.getState();
        //System.out.println(": " + xfer_state);
    }

    // Prints progress of a multiple file upload while waiting for it to finish.
    public static void showMultiUploadProgress(MultipleFileUpload multi_upload) {
        // print the upload's human-readable description
        System.out.println(multi_upload.getDescription());

        Collection<? extends Upload> sub_xfers = new ArrayList<Upload>();
        sub_xfers = multi_upload.getSubTransfers();

        do {
            System.out.println("\nSubtransfer progress:\n");
            for (Upload u : sub_xfers) {
                System.out.println("  " + u.getDescription());
                if (u.isDone()) {
                    TransferState xfer_state = u.getState();
                    System.out.println("  " + xfer_state);
                } else {
                    TransferProgress progress = u.getProgress();
                    double pct = progress.getPercentTransferred();
                    printProgressBar(pct);
                    System.out.println();
                }
            }

            // wait a bit before the next update.
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                return;
            }
        } while (multi_upload.isDone() == false);
        // print the final state of the transfer.
        TransferState xfer_state = multi_upload.getState();
        System.out.println("\nMultipleFileUpload " + xfer_state);
    }

    // prints a simple text progressbar: [#####     ]
    public static void printProgressBar(double pct) {
        // if bar_size changes, then change erase_bar (in eraseProgressBar) to
        // match.
        final int bar_size = 40;
        final String empty_bar = "                                        ";
        final String filled_bar = "########################################";
        int amt_full = (int) (bar_size * (pct / 100.0));
        System.out.format("  [%s%s]", filled_bar.substring(0, amt_full),
                empty_bar.substring(0, bar_size - amt_full));
    }

    // erases the progress bar.
    public static void eraseProgressBar() {
        // erase_bar is bar_size (from printProgressBar) + 4 chars.
        final String erase_bar = "\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b";
        System.out.format(erase_bar);
    }

    public void uploadWithProgress(String filePath, String bucketName, String keyName)
    {
        try {

            TransferManager tm = TransferManagerBuilder.standard()
                    .withS3Client(s3client)
                    .build();

            PutObjectRequest request = new PutObjectRequest(bucketName, keyName, new File(filePath));

            // To receive notifications when bytes are transferred, add a
            // ProgressListener to your request.
            request.setGeneralProgressListener(new ProgressListener() {
                public void progressChanged(ProgressEvent progressEvent) {
                    System.out.println("Transferred bytes: " + progressEvent.getBytesTransferred());
                }
            });
            // TransferManager processes all transfers asynchronously,
            // so this call returns immediately.
            Upload upload = tm.upload(request);

            // Optionally, you can wait for the upload to finish before continuing.
            upload.waitForCompletion();
        } catch (AmazonServiceException e) {
            // The call was transmitted successfully, but Amazon S3 couldn't process
            // it, so it returned an error response.
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (SdkClientException e) {
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            e.printStackTrace();
        }
    }
    public  void uploadFileWithListener(String file_path,
                                              String bucket_name, String key_prefix, boolean pause) {
        System.out.println("file: " + file_path +
                (pause ? " (pause)" : ""));

        String key_name = null;
        /*
        if (key_prefix != null) {
            key_name = key_prefix + '/' + file_path;
        } else {
            key_name = file_path;
        }
        */
        key_name = key_prefix;

        File f = new File(file_path);

        TransferManager xfer_mgr = TransferManagerBuilder.standard().withS3Client(s3client).build();
        try {
            Upload u = xfer_mgr.upload(bucket_name, key_name, f);
            // print an empty progress bar...
            printProgressBar(0.0);
            u.addProgressListener(new ProgressListener() {
                public void progressChanged(ProgressEvent e) {
                    double pct = e.getBytesTransferred() * 100.0 / e.getBytes();
                    eraseProgressBar();
                    printProgressBar(pct);
                }
            });
            // block with Transfer.waitForCompletion()
            waitForCompletion(u);
            // print the final state of the transfer.
            TransferState xfer_state = u.getState();
            System.out.println(": " + xfer_state);
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
        }
        xfer_mgr.shutdownNow();
    }

    public static void uploadDirWithSubprogress(String dir_path,
                                                String bucket_name, String key_prefix, boolean recursive,
                                                boolean pause) {
        System.out.println("directory: " + dir_path + (recursive ?
                " (recursive)" : "") + (pause ? " (pause)" : ""));

        TransferManager xfer_mgr = new TransferManager();
        try {
            MultipleFileUpload multi_upload = xfer_mgr.uploadDirectory(
                    bucket_name, key_prefix, new File(dir_path), recursive);
            // loop with Transfer.isDone()
            showMultiUploadProgress(multi_upload);
            // or block with Transfer.waitForCompletion()
            waitForCompletion(multi_upload);
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
        }
        xfer_mgr.shutdownNow();
    }

	public class S3object {
		private final SimpleStringProperty fileName;
		private final SimpleStringProperty fileSize;
		private final SimpleStringProperty fileType;
		private final SimpleStringProperty fileLastModified;
		private final SimpleStringProperty fileStorageClass;

		public S3object(String fileName, String fileSize, String fileType, String fileLastModified,
				String fileStorageClass) {
			this.fileName = new SimpleStringProperty(fileName);
			this.fileSize = new SimpleStringProperty(fileSize);
			this.fileType = new SimpleStringProperty(fileType);
			this.fileLastModified = new SimpleStringProperty(fileLastModified);
			this.fileStorageClass = new SimpleStringProperty(fileStorageClass);

		}

		public String getFileName() {
			return fileName.get();
		}

		public String getFileSize() {
			return fileSize.get();
		}

		public String getFileType() {
			return fileType.get();
		}

		public String getFileLastModified() {
			return fileLastModified.get();
		}

		public String getFileStorageClass() {
			return fileStorageClass.get();
		}

	}
}
