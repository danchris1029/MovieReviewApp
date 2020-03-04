import java.util.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.io.Closeable;
import java.io.File;
import java.net.URL;
import java.io.*;

public class ReviewHandler extends AbstractReviewHandler{
	private MovieReview mr;
	private List<MovieReview> movieReviews;
	private ReviewScore rs;
	private ReviewScore ps;
	
	
	/**
     * Searches the review database for reviews matching a given substring.
     * @param substring The substring to search for.
     * @return A list of review objects matching the search criterion.
     */
	@Override
	public List<MovieReview> searchBySubstring(String substring){
		List<MovieReview> mrs = new ArrayList<MovieReview>();
		String text = "";
		try {
		
			for(int i = 0; i < this.getDatabase().size(); i++)
			{
				if(this.getDatabase().get(i) != null){
					text = this.getDatabase().get(i).getText();
					if(text.indexOf(substring) != -1)
						mrs.add(this.getDatabase().get(i));
				}
			}
		}
		catch(NullPointerException npe) {
			System.out.println("Error finding reviews" + npe);
		}
		return mrs;
	}
	
	/**
     * Loads reviews from a given path and stores them into the database. 
     * If the given path is a .txt file, then a single review is loaded. 
     * Otherwise, if the path is a folder, all reviews in it are loaded.
     * This method calls the method classifyReview to classify each review.
     * @param filePath The path to the file (or folder) containing the review.
     * @param realClass The real class of the review (0 = Negative, 1 = Positive
     * 2 = Unknown).
     */
	@Override
	public void loadReviews(String filePath, int realClass) {
		File review = null;
		File f = null;
		Scanner reviewScnr = null;
		String[] reviewFiles = null;
		int uniqueID = 0;
		rs = ReviewScore.fromInteger(0);
		ps = ReviewScore.fromInteger(0);
		
		//if filePath is only a txt file
		if(filePath.indexOf(".txt") != -1) {
			try {
			System.out.println("Loading Review.");
			
			//if review is negative
			if(realClass == 0) {
				// Set review's real review score to negative
				rs = ReviewScore.fromInteger(0); 
				review = new File("data/Movie-reviews/neg/"+filePath);
			}
			//if review is positive
			else if(realClass == 1) {
				// Set review's real review score to positive
				rs = ReviewScore.fromInteger(1); 
				review = new File("data/Movie-reviews/pos/"+filePath);
			}
			
			//if review is unknown, look through both pos and neg folder
			else if(realClass == 2) {
				boolean filePathFound = false;
				// Set review's real review score to unknown
				rs = ReviewScore.fromInteger(2); 
				//tranverse through pos folder
				f = new File("material/data/Movie-reviews/pos");
				reviewFiles = f.list();
				for(int i = 0; i < reviewFiles.length;i++)
				{
					if(reviewFiles[i].equals(filePath))
					{
						review = new File("data/Movie-reviews/pos/"+filePath);
						filePathFound = true;
						break;
					}		
				}
				//transverse through neg folder
				if(filePathFound != true)
				{
					f = new File("data/Movie-reviews/neg");
					reviewFiles = f.list();
					for(int i = 0; i < reviewFiles.length;i++)
					{
						if(reviewFiles[i].equals(filePath))
						{
							review = new File("data/Movie-reviews/neg/"+filePath);
							filePathFound = true;
							break;
						}
					}
				}
			}
			
			//Move onto loading review
			String text = "";
			//If review file is not found, throw exception
			try {
			reviewScnr = new Scanner(review);
			}
			catch (FileNotFoundException fe) {
				System.out.println("File not found");
			}
		    
			while(reviewScnr.hasNext()) {
				text += reviewScnr.nextLine();
			}
			
			//Clasify predicted review score and set it to this MovieReview Object
			uniqueID = this.getUniqueId();
			mr = new MovieReview(uniqueID, filePath, text ,rs, ps);
			ps = this.classifyReview(mr);
			mr.setPredictedScore(ps);
			
			//A unique ID will be given to the current MovieReview Object
			this.getDatabase().put(uniqueID, mr);
			}
			catch(NullPointerException npe){
				System.out.println("Error: Path of file is not found");
				System.out.println("No review was loaded into the database");
			}
		}	
		
		//if filePath is a directory
		else {
			try {
			System.out.println("Loading Reviews.");
			String path = "";
			String text = "";
			//Does the path contain the negative folder or positive folder?
			//We must shape the directory path for suitable use
		    if(filePath.indexOf("/Movie-reviews/neg") != -1 || filePath.indexOf("\\Movie-reviews\\neg") != -1)
		    {
		    	// path is the negative review's directory
		    	path = "Movie-reviews/neg"; 
		    	// Set review's real review score to negative
		    	rs = ReviewScore.fromInteger(0); 
		    }
		    	
		    else if(filePath.indexOf("/Movie-reviews/pos") != -1 || filePath.indexOf("\\Movie-reviews\\pos") != -1)
		    {
		    	// path is the positve review's directory
		    	path = "Movie-reviews/pos"; 
		    	// Set review's real review score to positve
		    	rs = ReviewScore.fromInteger(1); 
		    }
		    
			f = new File(path);
			reviewFiles = f.list();
			for(int i = 0; i < reviewFiles.length;i++)
			{
				review = new File(path+"/"+reviewFiles[i]);//mod
				text = "";
				reviewScnr = new Scanner(review);
			    
				while(reviewScnr.hasNext()) {
					text += reviewScnr.nextLine();
				}
				
				//Clasify predicted review score and set it to this MovieReview Object
				uniqueID = this.getUniqueId(); 
				
				mr = new MovieReview(uniqueID, path+"/"+reviewFiles[i], text ,rs, ps);
				ps = this.classifyReview(mr);
				mr.setPredictedScore(ps);

				// Place MovieReview object into Database
				this.getDatabase().put(uniqueID, mr);
			}
			//not a txt file
			//Load multiple reviews into List<MovieReview>
			System.out.println("Finished loading reviews");
			}
			catch(NullPointerException npe ) {
				System.out.println("Error: Path of file is not found"); //mod
				System.out.println("No reviews were loaded into the database");
			}
			catch(FileNotFoundException fne) {
				
			}
		 }
	}
	
	
	
	/**
	 * Iterates through HashMap until Unique ID is found
	 * @return unique MovieReview id
	 */
	public int getUniqueId() {
		int idIteration = 0;
		while(this.getDatabase().containsKey(idIteration)) {
			idIteration++;
		}
		return idIteration;
	}
	
	
	/**
     * Reads a single review file and returns it as a MovieReview object. 
     * @param reviewFilePath A path to a .txt file containing a review.
     * @param realClass The real class entered by the user.
     * @return a MovieReview object.
     * @throws IOException if specified file cannot be opened.
     */
	@Override
	public MovieReview readReview(String reviewFilePath, int realClass) throws IOException{
		File review = new File(reviewFilePath);
		String text = "";
		Scanner reviewScnr = new Scanner(review);
	    
		while(reviewScnr.hasNext()) {
			text += reviewScnr.nextLine();
		}
		
		mr = new MovieReview(0, reviewFilePath, text ,rs, ps);
		return mr;
	}
	
	
	/**
     * Classifies a review as negative, or positive by using the text of the review.
     * It updates the predictedPolarity value of the review object and it also
     * returns the predicted polarity.
     * Note: the classification is achieved by counting positive and negative words
     * in the review text.
     * @param review A review object.
     * @return 0 = negative, 1 = positive.
     */
	@Override
	public  ReviewScore classifyReview(MovieReview review)
	{
		ReviewScore score;
		Scanner reviewScnr = new Scanner(review.getText());
		String currentWord;
		int posCount = 0;
		int negCount = 0;
		
		while(reviewScnr.hasNext()) {
			currentWord = reviewScnr.next();
			if(this.getPosWords().contains(currentWord))
				posCount++;
			if(this.getNegWords().contains(currentWord))
				negCount++;	
		}
		
		if(posCount > negCount)
			score = ReviewScore.fromInteger(1);
		else if(negCount > posCount)
			score = ReviewScore.fromInteger(0);
		else
			score = ReviewScore.fromInteger(2);
		return score;
	}
	
	
	/**
     * Deletes a review from the database, given its id.
     * @param id The id value of the review.
     */
	
	@Override
	public  void deleteReview(int id) {
		if(this.getDatabase().get(id) != null)
		{
			this.getDatabase().remove(id);
			System.out.println("Review with ID " + id + " was removed");
		}
			
		else
			System.out.println("ID does not exist in the database, no review was removed");
	}
	
	
	/**
     * Saves the database in the working directory as a text file (database.txt)
     * @throws java.io.IOException
     */
	
	@Override
	public void saveDB() throws IOException{
		int IDTag;
		File db = new File("classes/database.txt");
		db.delete();
		FileOutputStream dbFOS = new FileOutputStream("classes/database.txt");
		String filler = "";
		dbFOS.write(filler.getBytes());
		dbFOS.close();
		if(this.getDatabase().size()>0)
		{
			FileWriter dbWR = new FileWriter("classes/database.txt");
			for(int i = 0; i <= this.getDatabase().size();i++) {

				if(this.getDatabase().get(i) != null)
				{
					dbWR.write("ID$"+i+"\n");
					dbWR.write(this.getDatabase().get(i).getFilePath()+"\n");
					dbWR.write(this.getDatabase().get(i).getText()+"\n");
					dbWR.write(this.getDatabase().get(i).getRealScore()+"\n");
					dbWR.write(this.getDatabase().get(i).getPredictedScore()+"\n");
				}
			}
			dbWR.close();
			System.out.println("Reviews are saved to file \"database.txt\"");
		}
	}
	
	 /**
     * Loads review database from a file into the HashMap.
     * @throws java.io.IOException
     */
	
	@Override
	public void loadDB() throws IOException {
		File db;
		int ID;
		String reviewPath = "";
		String IDTag = "";
		Scanner dbScnr;
		
		try {
		// Check if database.txt exists within classes/project1 directory
		URL url = getClass().getResource(DATA_FILE_NAME); 
		if(url != null) {
			db = new File("classes/database.txt");
			dbScnr = new Scanner(db);
			try{
				
				while(dbScnr.hasNextLine()){
					IDTag = dbScnr.nextLine();
					// Get ID integer after $D$ tag
					ID = Integer.parseInt(IDTag.substring(3, IDTag.length()));     
					// Place other tags into MovieReview object								
					MovieReview mr = new MovieReview(ID, dbScnr.nextLine(), dbScnr.nextLine(), 		
							         ReviewScore.fromString(dbScnr.nextLine()),
							         ReviewScore.fromString(dbScnr.nextLine()));
					
					this.getDatabase().put(ID, mr);
					}
				System.out.println("here");
				dbScnr.close();
			}
			catch(NoSuchElementException NEE) {
				dbScnr.close();
			}
		}
		else {
			// If no database.txt file exists, then we create one and put in the classes/project1 directory
			System.out.println("No database located\nCreating database...\n"
			          		+	"Database created in same folder as class: " + getClass());
			FileOutputStream dbFOS = new FileOutputStream("classes/database.txt");
			String filler = "";
			dbFOS.write(filler.getBytes());
			dbFOS.close();
		}
		}
		catch(IOException IO){
			System.out.println("Problem scanning database, nothing loaded");
		}
	}

	/**
     * Searches the review database by id.
     * @param id The id to search for.
     * @return The review that matches the given id or null if the id does not 
     * exist in the database.
     */
	public  MovieReview searchById(int id) {
		mr = this.getDatabase().get(id);
		return mr;
	}
	
}