package com.theclcode.newsletter.aug19.imdbrecommender;

import java.util.Scanner;
import java.util.*;

//Incomplete
public class Solution {
    public static final int MAX_MOVIE = 5000;
    public static final int MAX_WATCH = 5000;
    public static final int MAX_USER = 1000;
    private static Map<Integer, User> users = new HashMap<>();
    private static Map<Integer, Movie> movies = new HashMap<>();
    private static int addedOn;

    /**************** START OF USER SOLUTION ****************/

    static void init()
    {
        users.clear();
        movies.clear();
        addedOn=1;
    }

    static void newMovie(int mid)
    {
        if(movies.get(mid)==null && movies.size()<MAX_MOVIE){
            Movie movie = new Movie(mid, new ArrayList<>(), addedOn++);
            movies.put(mid, movie);
        }
    }

    static void addUser(int uid)
    {
        if(users.get(uid)==null && users.size()<MAX_USER){
            User user = new User(uid, new HashMap<>());
            users.put(uid, user);
        }
    }

    static void watchMovie(int uid, int mid)
    {
        User user = users.get(uid);
        Movie movie = movies.get(mid);

        if(user!=null && movie!=null && user.moviesWatched.size()<MAX_WATCH){
            if(user.getMoviesWatched().containsKey(movie)){
                int movieWatchedCount = user.getMoviesWatched().get(movie);
                user.getMoviesWatched().put(movie, ++movieWatchedCount);
            } else {
                user.getMoviesWatched().put(movie, 1);
            }
            user.incrementWatchCounter();
            if(!movie.getWatchers().contains(user)){
                movie.getWatchers().add(user);
            }
        }
    }

    static int getRecommendedMovie(int uid)
    {
        int numberOfTopUsers = users.size()*0.01 > 1 ? (int)(users.size()*0.01) : 1;
        User[] similarUsers = new User[numberOfTopUsers];
        User user = users.get(uid);
        Map<User, Integer> candidateSimilarUsers = new HashMap<>();

        findSimilarTasteGroup(user, similarUsers, candidateSimilarUsers);
        if(candidateSimilarUsers.isEmpty()) {
            return -1;
        }
        Map<Movie, Integer> candidateRecommendedMovies = getCandidateRecommendedMovies(user, similarUsers);
        int topMovie = getTopMovie(candidateRecommendedMovies);
        return topMovie;
    }

    private static User[] sortMap(Map<User, Integer> candidateSimilarUsers) {
        User[] candidateSimilarUsersArray = new User[candidateSimilarUsers.size()];
        int counter=0;
        for(Map.Entry<User, Integer> candidateSimilarUser : candidateSimilarUsers.entrySet()){
            candidateSimilarUsersArray[counter] = candidateSimilarUser.getKey();
            counter++;
        }

        for(int i=1; i<candidateSimilarUsers.size(); i++){
            int temp = candidateSimilarUsers.get(candidateSimilarUsersArray[i]);
            User tempUser = candidateSimilarUsersArray[i];
            int j = i-1;
            while(j >= 0 && temp < candidateSimilarUsers.get(candidateSimilarUsersArray[j])){
                candidateSimilarUsersArray[i] = candidateSimilarUsersArray[j];
                j--;
            }
            candidateSimilarUsersArray[j+1] = tempUser;
        }
        return candidateSimilarUsersArray;
    }

    private static void findSimilarTasteGroup(User user, User[] similarUsers, Map<User, Integer> candidateSimilarUsers) {
        for(Map.Entry<Movie, Integer> movie : user.getMoviesWatched().entrySet()){
             for(User similarUser: movie.getKey().getWatchers()){
                 if(similarUser==user){
                     continue;
                 }
                if(candidateSimilarUsers.containsKey(similarUser)){
                    int numOfSameMoviesWatched = candidateSimilarUsers.get(similarUser);
                    candidateSimilarUsers.put(similarUser, numOfSameMoviesWatched+1);
                } else {
                    candidateSimilarUsers.put(similarUser, 1);
                }
             }
        }

        int similarUsersSize = similarUsers.length;
        if(!candidateSimilarUsers.isEmpty()){
            User[] candidateSimilarUsersArray = sortMap(candidateSimilarUsers);
            Map<User, Integer> candidateTopUsers = checkIfMultipleTopUsersAndGetTopUsers(
                    candidateSimilarUsersArray, candidateSimilarUsers, false);
            if(candidateTopUsers.size()>similarUsersSize){
                candidateSimilarUsers.clear();
                getUsersByWatchCount(similarUsers, candidateSimilarUsers, candidateTopUsers);
            } else{
                buildTopUsers(similarUsers, candidateSimilarUsers,false);
            }
        } else {
            getUsersByWatchCount(similarUsers, candidateSimilarUsers, null);
        }
    }

    private static void getUsersByWatchCount(User[] similarUsers,
        Map<User, Integer> candidateSimilarUsers, Map<User, Integer> candidateMultipleTopSimilarUsers) {

        if(candidateMultipleTopSimilarUsers==null){
            for(Map.Entry<Integer, User> similarUser : users.entrySet()){
                candidateSimilarUsers.put(similarUser.getValue(), similarUser.getValue().getWatchCounter());
            }
        } else {
            for(Map.Entry<User, Integer> topUser : candidateMultipleTopSimilarUsers.entrySet()){
                candidateSimilarUsers.put(topUser.getKey(), topUser.getKey().getWatchCounter());
            }
        }

        int similarUsersSize = similarUsers.length;
        if(!candidateSimilarUsers.isEmpty()){
            User[] candidateSimilarUsersArray = sortMap(candidateSimilarUsers);
            Map<User, Integer> candidateTopUsers = checkIfMultipleTopUsersAndGetTopUsers(
                    candidateSimilarUsersArray, candidateSimilarUsers, false);
            if(candidateTopUsers.size()>similarUsersSize){
                candidateSimilarUsers.clear();
                getUsersById(similarUsers, candidateSimilarUsers, candidateTopUsers);
            } else{
                buildTopUsers(similarUsers, candidateSimilarUsers,false);
            }

        } else {
            getUsersById(similarUsers, candidateSimilarUsers, null);
        }
    }

    private static void getUsersById(User[] similarUsers, Map<User,
        Integer> candidateSimilarUsers, Map<User, Integer> candidateMultipleTopSimilarUsers) {
        if(candidateMultipleTopSimilarUsers==null){
            for(Map.Entry<Integer, User> similarUser : users.entrySet()){
                candidateSimilarUsers.put(similarUser.getValue(), similarUser.getValue().getUid());
            }
        } else {
            for(Map.Entry<User, Integer> topUser : candidateMultipleTopSimilarUsers.entrySet()){
                candidateSimilarUsers.put(topUser.getKey(), topUser.getKey().getUid());
            }
        }
        buildTopUsers(similarUsers, candidateSimilarUsers,true);
    }

    private static Map<User, Integer> checkIfMultipleTopUsersAndGetTopUsers(
            User[] candidateSimilarUsersArray, Map<User, Integer> candidateSimilarUsers, boolean isAscending) {

        Map<User, Integer> candidateTopUsers = new HashMap<>();
        int highestValue = 0;
        if(isAscending){
            highestValue = candidateSimilarUsers.get(candidateSimilarUsersArray[0]);
        } else {
            highestValue = candidateSimilarUsers.get(candidateSimilarUsersArray[candidateSimilarUsersArray.length-1]);
        }
        for(Map.Entry<User, Integer> candidateTopUser : candidateSimilarUsers.entrySet()){
            if(candidateTopUser.getValue()==highestValue){
                candidateTopUsers.put(candidateTopUser.getKey(), candidateTopUser.getValue());
            }
        }
        return candidateTopUsers;
    }

    private static void buildTopUsers(User[] similarUsers, Map<User, Integer> candidateSimilarUsers, boolean isAscending) {
        User[] candidateSimilarUsersArray = sortMap(candidateSimilarUsers);
        if(isAscending){
            for(int i=0; i<similarUsers.length; i++){
                similarUsers[i] = candidateSimilarUsersArray[i];
            }
        } else {
            for(int i=0,j=candidateSimilarUsersArray.length-1; i<similarUsers.length; i++){
                similarUsers[i] = candidateSimilarUsersArray[j];
            }
        }
    }

    private static int getTopMovie(Map<Movie, Integer> candidateRecommendedMovies) {
        Movie mostWatched = null;
        int watchCount = 0;

        for(Map.Entry<Movie, Integer> recommendedMovie : candidateRecommendedMovies.entrySet()){
            if(mostWatched==null){
                mostWatched=recommendedMovie.getKey();
                watchCount=recommendedMovie.getValue();
                continue;
            }
            //Most watched within the taste group
            if(recommendedMovie.getValue()>watchCount){
                mostWatched = recommendedMovie.getKey();
                watchCount = recommendedMovie.getValue();
            }
            else if(recommendedMovie.getValue()==watchCount){
                if(recommendedMovie.getKey().getAdded()>mostWatched.getAdded()){
                    mostWatched=recommendedMovie.getKey();
                    watchCount= recommendedMovie.getValue();
                }
            }

        }
        if(mostWatched!=null){
            return mostWatched.getMid();
        }
        return -1;
    }

    private static Map<Movie, Integer> getCandidateRecommendedMovies(User user, User[] similarUsers) {
        Map<Movie, Integer> movieList = new HashMap<>();
        for(User similarUser : similarUsers){
            for(Map.Entry<Movie, Integer> movieWatched: similarUser.getMoviesWatched().entrySet()){
                Movie movie = movieWatched.getKey();
                if(user.getMoviesWatched().get(movie)==null){
                    if(movieList.containsKey(movie)){
                        int totalWatchCount = movieList.get(movie);
                        movieList.put(movie, totalWatchCount+movieWatched.getValue());
                    } else {
                        movieList.put(movie, movieWatched.getValue());
                    }
                }
            }
        }
        return movieList;
    }

    /***************** END OF USER SOLUTION *****************/


    public static final int CMD_INIT = 10;
    public static final int CMD_MOVIE = 20;
    public static final int CMD_USER = 30;
    public static final int CMD_WATCH = 40;
    public static final int CMD_RECOMMEND = 50;

    static Scanner sc;

    static void run() {
        int  m, cmd, arg1, arg2, ret;

        m = sc.nextInt();

        while (m-- > 0) {
            cmd = sc.nextInt();

            switch (cmd) {
                case CMD_INIT:
                    init();

                    break;

                case CMD_MOVIE:
                    arg1 = sc.nextInt();
                    newMovie(arg1);

                    break;

                case CMD_USER:
                    arg1 = sc.nextInt();
                    addUser(arg1);

                    break;

                case CMD_WATCH:
                    arg1 = sc.nextInt();
                    arg2 = sc.nextInt();
                    watchMovie(arg1, arg2);

                    break;

                case CMD_RECOMMEND:
                    arg1 = sc.nextInt();
                    ret = getRecommendedMovie(arg1);

                    System.out.printf("%d\n", ret);

                    break;
            }
        }
    }

    public static void main(String[] args) {
        int T, ANS, tc;

        sc = new Scanner(System.in);

        T = sc.nextInt();
        ANS = sc.nextInt();

        for (tc = 1; tc <= T; ++tc) {
            if(tc==3){
                String x = "null";
            }
            System.out.printf("Case #%d:\n", tc);
            run();
        }
    }

    public static class Movie {
        private int mid;
        private List<User> watchers;
        private int added;

        public Movie(int mid, List<User> watchers, int added) {
            this.added = added;
            this.mid = mid;
            this.watchers = watchers;
        }

        public int getAdded() {
            return added;
        }

        public void setAdded(int added) {
            this.added = added;
        }

        public int getMid() {
            return mid;
        }

        public void setMid(int mid) {
            this.mid = mid;
        }

        public List<User> getWatchers() {
            return watchers;
        }

        public void setWatchers(List<User> watchers) {
            this.watchers = watchers;
        }
    }

    public static class User {
        private int uid;
        //Movie, Number of times they watched the movie.
        private Map<Movie, Integer> moviesWatched;

        private int watchCounter=0;

        public User(int uid, Map<Movie, Integer> moviesWatched) {
            this.uid = uid;
            this.moviesWatched = moviesWatched;
        }

        public int getUid() {
            return uid;
        }

        public void setUid(int uid) {
            this.uid = uid;
        }

        public Map<Movie, Integer> getMoviesWatched() {
            return moviesWatched;
        }

        public void setMoviesWatched(Map<Movie, Integer> moviesWatched) {
            this.moviesWatched = moviesWatched;
        }

        public int getWatchCounter() {
            return watchCounter;
        }

        public void incrementWatchCounter() {
            this.watchCounter = getWatchCounter()+1;
        }
    }
}
