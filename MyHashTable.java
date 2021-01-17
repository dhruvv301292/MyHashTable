/**
 * 17-683 Data Structures for Application Programmers.
 * Homework Assignment 4: HashTable Implementation with linear probing
 *
 * Andrew ID: dvashish
 * @author Dhruv Vashisht
 */
public class MyHashTable implements MyHTInterface {
    /**
     * The DataItem array of the table.
     */
    private DataItem[] hashArray;

    /**
     * The size of the hashArray.
     */
    private int size = 0;

    /**
     * count of number of collisions.
     */
    private int numOfCollisions = 0;

    /**
     * constant string to replace deleted values from Hash Array.
     */
    private static final DataItem DELETED = new DataItem("DELETED");

    /**
     * constant int for default capacity.
     */
    private static final int DEFAULT_CAP = 10;
    /**
     * Default Constructor.
     */
    public MyHashTable() {
        hashArray = new DataItem[DEFAULT_CAP];
    }

    /**
     * Constructor to initialize hashArray with custom initial capacity.
     * @param initialCap Initial Capacity of hashArray
     */
    public MyHashTable(int initialCap) {
        if (initialCap <= 0) {
            throw new RuntimeException("Initial Capacity of Hash Table cannot be 0 or less.");
        }
        hashArray = new DataItem[initialCap];
    }

    /**
     * Instead of using String's hashCode, you are to implement your own here.
     * You need to take the table length into your account in this method.
     *
     * In other words, you are to combine the following two steps into one step.
     * 1. converting Object into integer value
     * 2. compress into the table using modular hashing (division method)
     *
     * Helper method to hash a string for English lowercase alphabet and blank,
     * we have 27 total. But, you can assume that blank will not be added into
     * your table. Refer to the instructions for the definition of words.
     *
     * For example, "cats" : 3*27^3 + 1*27^2 + 20*27^1 + 19*27^0 = 60,337
     *
     * But, to make the hash process faster, Horner's method should be applied as follows;
     *
     * var4*n^4 + var3*n^3 + var2*n^2 + var1*n^1 + var0*n^0 can be rewritten as
     * (((var4*n + var3)*n + var2)*n + var1)*n + var0
     *
     * Note: You must use 27 for this homework.
     *
     * However, if you have time, I would encourage you to try with other
     * constant values than 27 and compare the results but it is not required.
     * @param input input string for which the hash value needs to be calculated
     * @return int hash value of the input string
     */
    private int hashFunc(String input) {
        char[] inputArray = input.toCharArray();
        int hash = inputArray[0] - 96;
        for (int i = 1; i < inputArray.length; i++) {
            if (hash < 0) {
                hash = hash * -1;
            }
            hash = ((hash % hashArray.length) * 27) + (inputArray[i] - 96);
        }
        return hash;
    }

    /**
     * doubles array length and rehash items whenever the load factor is reached.
     * Note: do not include the number of deleted spaces to check the load factor.
     * Remember that deleted spaces are available for insertion.
     */
    private void rehash() {
        int arraySize = 2 * hashArray.length;
        int rehashCount = 0;
        while (!checkPrime(arraySize)) {
            arraySize++;
        }
        DataItem[] temp = hashArray;
        hashArray = new DataItem[arraySize];
        for (DataItem d: temp) {
            if (d != null && d != DELETED) {
                rehashCount++;
                for (int i = 0; i < d.frequency; i++) {
                    insert(d.value);
                }
            }
        }
        System.out.println("Rehashing " + rehashCount + " items, new length is " + arraySize);
    }

    /**
     * helper method to check if new arraySize is prime or not.
     * @param arraySize Size of the array
     * @return true if arraySize is Prime
     */
    private boolean checkPrime(int arraySize) {
        if (arraySize <= 1) {
            return false;
        }
        for (int i = 2; i < arraySize; i++) {
            if (arraySize % i == 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * private static data item nested class.
     */
    private static class DataItem {
        /**
         * String value.
         */
        private String value;
        /**
         * String value's frequency.
         */
        private int frequency;

        /**
         * Constructor for DataItem.
         * @param val String value
         */
        DataItem(String val) {
            value = val;
            frequency = 1;
        }

        /**
         * Returns string representation of DataItem object.
         * @return String value and frequency of DataItem Object.
         */
        @Override
        public String toString() {
            if (value.equals("DELETED")) {
                return "#DEL#";
            }
            return "[" + value + ", " + frequency + "]";
        }
    }

    /**
     * Insert method to insert string into hash Table.
     */
    @Override
    public void insert(String value) {
        if (isWord(value)) {
            if (find(value) >= 0) {
                int hashVal = find(value);
                hashArray[hashVal].frequency++;
            } else {
                int hashVal = hashValue(value);
                boolean collisionFlag = false;
                while (hashArray[hashVal] != null  && hashArray[hashVal] != DELETED) { //jab tak null ya deleted ni aa jata
                    if (!collisionFlag && hashValue(value) == hashValue(hashArray[hashVal].value)) { //agar collisionflag set ni hai and current index ki hashvalue entry wale se same hai
                        numOfCollisions++;
                        collisionFlag = true;
                    }
                    hashVal++;
                    hashVal = hashVal % hashArray.length;
                }
                if (hashArray[hashVal] == DELETED && findHash(value)) {//agar deleted encounter hua and aage kahi same hashvalue wala word hai
                    numOfCollisions++;
                    collisionFlag = true;
                }
                hashArray[hashVal] = new DataItem(value);
                size++;
            }
            if ((float) size / hashArray.length > 0.5) {
                numOfCollisions = 0;
                size = 0;
                rehash();
            }
        }
    }

    /**
     * Simple private helper method to validate a word.
     * @param text text to check
     * @return true if valid, false if not
     */
    private boolean isWord(String text) {
        if (text != null && text.trim() != "") {
            return text.matches("[a-zA-Z]+");
        }
        return false;
    }

    /**
     * method to return size of the Hash Table.
     * @return size integer size of hash table
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * method to display elements of Hash Table.
     */
    @Override
    public void display() {
        StringBuilder displayBuilder = new StringBuilder();
        for (DataItem d: hashArray) {
            if (d == null) {
                displayBuilder.append("** ");
            } else {
                displayBuilder.append(d);
                displayBuilder.append(" ");
            }
        }
        displayBuilder.append("\n");
        System.out.println(displayBuilder.toString());
    }

    /**
     * method to check if key is present in the Hash Table.
     * @param key key to check
     * @return true if present, false if not
     */
    @Override
    public boolean contains(String key) {
        int hashVal  = hashValue(key);
        int count = 0;
        while (hashArray[hashVal] != null) {
            if (hashArray[hashVal].value.equals(key)) {
                return true;
            }
            hashVal++;
            hashVal = hashVal % hashArray.length;
            count++;
            if (count > hashArray.length) {
                break;
            }
        }
        return false;
    }

    /**
     * private helper method to find the index of existing element in array.
     * @param key String to be found
     * @return index of key string if found.
     */
    private int find(String key) {
        int hashVal  = hashValue(key);
        int count = 0;
        while (hashArray[hashVal] != null) {
            if (hashArray[hashVal].value.equals(key)) {
                return hashVal;
            }
            hashVal++;
            hashVal = hashVal % hashArray.length;
            count++;
            if (count > hashArray.length) {
                break;
            }
        }
        return -1;
    }

    /**
     * private helper method to find different string element with same hash value as key String.
     * @param key the string value to check for
     * @return true if hash value exists in array.
     */
    private boolean findHash(String key) {
        int hashVal = hashValue(key);
        int count = 0;
        while (hashArray[hashVal] != null) {
            if (hashValue(key) == hashValue(hashArray[hashVal].value)) {
                if (!key.equals(hashArray[hashVal].value)) {
                    return true;
                }
            }
            hashVal++;
            hashVal = hashVal % hashArray.length;
            count++;
            if (count > hashArray.length) {
                break;
            }
        }
        return false;
    }

    /**
     * method to return number of Collisions.
     * @return integer number of collisions
     */
    @Override
    public int numOfCollisions() {
        return numOfCollisions;
    }

    /**
     * method to return hash Value of string.
     * @param value to calculate hash Value of.
     * @return integer hash value of String.
     */
    @Override
    public int hashValue(String value) {
        return hashFunc(value) % hashArray.length;
    }

    /**
     * method to return frequency of string key.
     * @param key value to find frequency of.
     * @return integer frequency of String key.
     */
    @Override
    public int showFrequency(String key) {
        if (find(key) >= 0) {
            int hashVal = find(key);
            return hashArray[hashVal].frequency;
        }
        return 0;
    }

    /**
     * method to remove string key from Hash Table.
     * @param key value to remove from HashTable.
     * @return String value of removed String.
     */
    @Override
    public String remove(String key) {
        if (find(key) >= 0) {
            int hashVal = find(key);
            DataItem tmpDataItem = hashArray[hashVal];
            hashArray[hashVal] = DELETED;
            hashArray[hashVal].frequency = 0;
            size = size - tmpDataItem.frequency;
            return tmpDataItem.value;
        }
        return null;
    }
}
