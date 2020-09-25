package application;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class MainController implements Initializable{

	// Table Section

	@FXML private TableView<PDFFile> leftTable;
	@FXML private TableColumn<PDFFile, String> leftFileName;
	@FXML private TableColumn<PDFFile, String> leftFileSize;
	@FXML private TableColumn<PDFFile, String> leftFileModified;

	@FXML private TableView<PDFFile> rightTable;
	@FXML private TableColumn<PDFFile, String> rightFileName;
	@FXML private TableColumn<PDFFile, String> rightFileSize;
	@FXML private TableColumn<PDFFile, String> rightFileModified;

	// Text Fields

	@FXML private TextField ruleFile;
	@FXML private TextField resultFolder;
	@FXML private TextField mask;

	// Buttons

	@FXML Button pickRuleFile;
	@FXML Button pickResultFolder;
	@FXML Button startButton;

	@FXML ListView console;

	ObservableList<PDFFile> list = FXCollections.observableArrayList();

	Stage primaryStage;

	Thread thread;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		initalizeCols();
		loadData();

	}

	private void initalizeCols()
	{

		leftFileName.setCellValueFactory(new PropertyValueFactory<>("FileName"));
		leftFileSize.setCellValueFactory(new PropertyValueFactory<>("FileSize"));
		leftFileModified.setCellValueFactory(new PropertyValueFactory<>("FileModified"));


		rightFileName.setCellValueFactory(new PropertyValueFactory<>("FileName"));
		rightFileSize.setCellValueFactory(new PropertyValueFactory<>("FileSize"));
		rightFileModified.setCellValueFactory(new PropertyValueFactory<>("FileModified"));

	}
	private void loadData()
	{
		/*
		list.removeAll(list);
		list.addAll(new Contacts("Sam","Home","909-3993-39393"));
		list.addAll(new Contacts("Ttest","Home","909-3993-39393"));
		list.addAll(new Contacts("dssd","Home","909-3993-39393"));
		contactView.getItems().addAll(list);
		*/
	}

	public void handleDropLeft(DragEvent event)
	{
		Hashtable<String,Boolean> fileHash = new Hashtable();
		leftTable.getItems().clear();
		leftTable.refresh();
		List<File> files = event.getDragboard().getFiles();
		// Process the file/files
		files.forEach(file -> {
				if (file.isDirectory())
				{
					String rootPath = file.getAbsolutePath();
					try {
						Files.walk(Paths.get(rootPath))
						.filter(Files::isRegularFile)
						.forEach(filePath -> {
							String absPath = filePath.toAbsolutePath().toString();
							fileHash.put(filePath.toAbsolutePath().toString(), false);
						}

						);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if (file.isFile())
					fileHash.put(file.getAbsolutePath(),false);

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
	                    	PDFFile fileEntry = new PDFFile(file.getAbsolutePath(),  String.valueOf(file.length()),String.valueOf(file.lastModified()));
	                    	leftTable.getItems().add(fileEntry);
	                    }
	                };
	                Platform.runLater(updater);
		        });
				fileHash.clear();
			}
		});


		thread.start();

	}
	public void handleDropRightt(DragEvent event)
	{
		Hashtable<String,Boolean> fileHash = new Hashtable();
		rightTable.getItems().clear();
		rightTable.refresh();
		List<File> files = event.getDragboard().getFiles();
		// Process the file/files
		files.forEach(file -> {
				if (file.isDirectory())
				{
					String rootPath = file.getAbsolutePath();
					try {
						Files.walk(Paths.get(rootPath))
						.filter(Files::isRegularFile)
						.forEach(filePath -> {
							String absPath = filePath.toAbsolutePath().toString();
							fileHash.put(filePath.toAbsolutePath().toString(), false);
						}

						);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if (file.isFile())
					fileHash.put(file.getAbsolutePath(),false);

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
	                    	PDFFile fileEntry = new PDFFile(file.getAbsolutePath(),  String.valueOf(file.length()),String.valueOf(file.lastModified()));
	                    	rightTable.getItems().add(fileEntry);
	                    }
	                };
	                Platform.runLater(updater);
		        });
				fileHash.clear();
			}
		});


		thread.start();
	}
	public void handeFolderSelect()
	{
		DirectoryChooser directoryChooser = new DirectoryChooser();
		File selectedDirectory = directoryChooser.showDialog(primaryStage);
		resultFolder.setText(selectedDirectory.getAbsolutePath());

	}

	public void handeRuleFileSelect()
	{


		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select Rule File");
		File f = fileChooser.showOpenDialog(primaryStage);

		ruleFile.setText(f.getAbsolutePath());

	}
	public void setStage(Stage stage)
	{
		this.primaryStage = stage;
	}
	public void handleDragOver(DragEvent event)
	{
		if (event.getDragboard().hasFiles())
		{
			event.acceptTransferModes(TransferMode.COPY);
		}
	}
	public class PDFFile {

		private final String fileName;
		private final String fileSize;
		private final String fileLastModified;


		public PDFFile(String fileName, String fileSize, String fileLastModified) {
			this.fileName = fileName;
			this.fileSize = fileSize;
			this.fileLastModified =fileLastModified;

		}


		public String getFileName() {
			return fileName;
		}


		public String getFileSize() {
			return fileSize;
		}


		public String getFileLastModified() {
			return fileLastModified;
		}


	}
}
