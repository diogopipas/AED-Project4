package aed.tables;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

public class OpenAddressingHashTable<Key, Value> {

    private static int[] primes = {
            37, 79, 163, 331,
            673, 1361, 2729, 5471, 10949,
            21911, 43853, 87719, 175447, 350899,
            701819, 1403641, 2807303, 5614657,
            11229331, 22458671, 44917381, 89834777, 179669557
    };

    private int m;
    private int primeIndex;
    private int size;
    private float loadFactor;
    private Key[] keys;
    private Value[] values;
    private int deletedNotRemoved;

    @SuppressWarnings("unchecked")
    private OpenAddressingHashTable(int primeIndex) {
        this.primeIndex = primeIndex;
        this.m = primes[primeIndex];
        this.size = 0;
        this.loadFactor = 0.0f;
        this.deletedNotRemoved = 0;
        this.keys = (Key[]) new Object[this.m];
        this.values = (Value[]) new Object[this.m];
    }

    public OpenAddressingHashTable() {
        this(0);
    }

    public int size() {
        return this.size;
    }

    public int getCapacity() {
        return this.m;
    }

    public float getLoadFactor() {
        return this.loadFactor;
    }

    public int getDeletedNotRemoved() {
        return this.deletedNotRemoved;
    }

    public boolean containsKey(Key k) {
        return get(k) != null;
    }

    private int hash1(Key k) {
        return (k.hashCode() & 0x7fffffff) % this.m;
    }

    private int hash2(Key k) {
        if (this.primeIndex == 0) {
            // For the first prime, use a fixed smaller prime to avoid step size of 0
            return 17 - (hash1(k) % 17);
        } else {
            // Use the previous prime in the primes array
            int p = primes[this.primeIndex - 1];
            return p - (hash1(k) % p);
        }
    }


    public Value get(Key k) {
        if (k == null) return null;
        int h1 = hash1(k);
        int h2 = hash2(k);
        int i = h1;

        for (int step = 0; step < m; step++) {
            if (keys[i] == null) {
                return null;
            }
            if (keys[i].equals(k)) {
                return values[i];
            }
            i = (h1 + (step + 1) * h2) % m;
        }
        return null;
    }

    private void resize(int primeIndex) {
        if (primeIndex < 0 || primeIndex >= primes.length) return;

        OpenAddressingHashTable<Key, Value> aux = new OpenAddressingHashTable<>(primeIndex);

        for (int i = 0; i < this.m; i++) {
            if (keys[i] != null) {
                aux.put(keys[i], values[i]);
            }
        }

        this.primeIndex = aux.primeIndex;
        this.keys = aux.keys;
        this.values = aux.values;
        this.m = aux.m;
        this.size = aux.size;
        this.loadFactor = aux.loadFactor;
        this.deletedNotRemoved = 0; // Reset after rebuilding
    }

    // Inserts or updates a key-value pair using double hashing
    public void put(Key k, Value v) {
        if (k == null) return;

        if (this.loadFactor >= 0.5f) {
            resize(this.primeIndex + 1);
        }

        if (v == null) {
            delete(k);
            return;
        }

        int h1 = hash1(k);
        int h2 = hash2(k);
        int i = h1;

        for (int step = 0; step < m; step++) {
            if (keys[i] == null) {
                // Insert the new key-value pair here
                keys[i] = k;
                values[i] = v;
                size++;
                loadFactor = (float) size / m;
                return;
            }
            if (keys[i].equals(k)) {
                // Update the existing value
                values[i] = v;
                return;
            }
            i = (i + h2) % m;
        }
    }


    public void delete(Key k) {
        if (k == null) return;

        int h1 = hash1(k);
        int h2 = hash2(k);
        int i = h1;

        // Step 1: Find the key to delete
        for (int step = 0; step < m; step++) {
            if (keys[i] == null) {
                return; // Key not found
            }
            if (keys[i].equals(k)) {
                // Key found, delete it
                keys[i] = null;
                values[i] = null;
                size--; // Decrease the size after removal
                deletedNotRemoved++; // Increment deletedNotRemoved

                // Step 2: Rehash subsequent keys in the cluster
                int j = (i + h2) % m;
                while (keys[j] != null) {
                    Key rehashKey = keys[j];
                    Value rehashValue = values[j];
                    keys[j] = null;
                    values[j] = null;
                    size--; // Decrement size before reinsertion

                    put(rehashKey, rehashValue);
                    j = (j + h2) % m;
                }

                // Step 3: Adjust load factor and check for resizing
                loadFactor = (float) size / m;
                if (loadFactor < 0.125f) {
                    resize(primeIndex - 1);
                }

                // Calculate the percentage of deleted keys
                float deletedPercentage = (float) deletedNotRemoved / m;
                if (deletedPercentage >= 0.2f) {
                    // Rebuild the table to clean up deleted slots
                    resize(primeIndex);
                }

                return; // Deletion and rehashing complete
            }
            // Continue probing using double hashing
            i = (i + h2) % m;
        }
    }

    // Returns an iterable of all active keys in the table
    @SuppressWarnings("unchecked")
    public Iterable<Key> keys() {
        ArrayList<Key> keysArr = new ArrayList<>();
        for (int i = 0; i < m; i++) {
            if (keys[i] != null) {
                keysArr.add(keys[i]);
            }
        }
        return keysArr;
    }

    // Main method with comprehensive test cases
    public static void main(String[] args) {
        OpenAddressingHashTable<String, Integer> hashTable = new OpenAddressingHashTable<>();

        /*
        System.out.println("===== Test 1: Insertion and Retrieval =====");
        hashTable.put("apple", 1);
        hashTable.put("banana", 2);
        hashTable.put("cherry", 3);
        hashTable.put("date", 4);
        hashTable.put("elderberry", 5);

        System.out.println("Size after insertions: " + hashTable.size()); // Expected: 5
        System.out.println("Get 'apple': " + hashTable.get("apple"));     // Expected: 1
        System.out.println("Get 'banana': " + hashTable.get("banana"));   // Expected: 2
        System.out.println("Get 'cherry': " + hashTable.get("cherry"));   // Expected: 3
        System.out.println("Get 'date': " + hashTable.get("date"));       // Expected: 4
        System.out.println("Get 'elderberry': " + hashTable.get("elderberry")); // Expected: 5

        System.out.println("\n===== Test 2: Updating Values =====");
        hashTable.put("banana", 20);
        System.out.println("Updated 'banana': " + hashTable.get("banana")); // Expected: 20

        System.out.println("\n===== Test 3: Deletion =====");
        hashTable.delete("cherry");
        System.out.println("Size after deleting 'cherry': " + hashTable.size()); // Expected: 4
        System.out.println("Contains 'cherry': " + hashTable.containsKey("cherry")); // Expected: false
        System.out.println("Get 'cherry': " + hashTable.get("cherry")); // Expected: null

        System.out.println("\n===== Test 4: Handling Non-Existent Keys =====");
        System.out.println("Get 'fig' (not inserted): " + hashTable.get("fig")); // Expected: null
        hashTable.delete("fig"); // Should not affect the table
        System.out.println("Size after attempting to delete 'fig': " + hashTable.size()); // Expected: 4

        System.out.println("\n===== Test 5: Resizing Mechanism =====");
        // Insert more elements to trigger resizing
        hashTable.put("fig", 6);
        hashTable.put("grape", 7);
        hashTable.put("honeydew", 8);
        hashTable.put("kiwi", 9);
        hashTable.put("lemon", 10);
        hashTable.put("mango", 11);
        hashTable.put("nectarine", 12);
        hashTable.put("orange", 13);
        hashTable.put("papaya", 14);
        hashTable.put("quince", 15);

        System.out.println("Size after multiple insertions: " + hashTable.size()); // Expected: 14
        System.out.println("Capacity after resizing: " + hashTable.getCapacity()); // Should have increased
        System.out.println("Load factor: " + hashTable.getLoadFactor()); // Should be <= 0.5

        // Verify all elements are accessible
        String[] keysToCheck = {"apple", "banana", "date", "elderberry", "fig", "grape", "honeydew",
                "kiwi", "lemon", "mango", "nectarine", "orange", "papaya", "quince"};
        boolean allFound = true;
        for (String key : keysToCheck) {
            if (hashTable.get(key) == null) {
                System.out.println("Error: Missing key '" + key + "'");
                allFound = false;
            }
        }
        if (allFound) {
            System.out.println("All keys are correctly accessible after resizing.");
        }

        System.out.println("\n===== Test 6: Iteration Over Keys =====");
        System.out.println("Keys in the hash table:");
        for (String key : hashTable.keys()) {
            System.out.println(key + " => " + hashTable.get(key));
        }

        System.out.println("\n===== Test 7: Edge Cases =====");
        // Attempt to delete all keys
        for (String key : keysToCheck) {
            hashTable.delete(key);
        }
        System.out.println("Size after deleting all keys: " + hashTable.size()); // Expected: 0
        System.out.println("Load factor after deletions: " + hashTable.getLoadFactor()); // Expected: 0.0

        // Attempt to delete from empty table
        hashTable.delete("apple"); // Should not cause any issues
        System.out.println("Size after attempting to delete from empty table: " + hashTable.size()); // Expected: 0

        // Insert null key or value (if allowed)
        try {
            hashTable.put(null, 100);
            System.out.println("Inserted null key with value 100.");
            System.out.println("Get null key: " + hashTable.get(null)); // Expected: 100
        } catch (Exception e) {
            System.out.println("Error inserting null key: " + e.getMessage());
        }

        try {
            hashTable.put("nullValue", null);
            System.out.println("Inserted 'nullValue' key with null value.");
            System.out.println("Get 'nullValue': " + hashTable.get("nullValue")); // Expected: null
        } catch (Exception e) {
            System.out.println("Error inserting null value: " + e.getMessage());
        }

        System.out.println("\n===== Final State of Hash Table =====");
        System.out.println("Size: " + hashTable.size());
        System.out.println("Keys:");
        for (String key : hashTable.keys()) {
            System.out.println(key + " => " + hashTable.get(key));
        }

         */
        for(int i = 0; i < 1011; i++){
            hashTable.put("" + i, i);
        }

        for(int j = 0; j < 163; j++){
            hashTable.delete("" + j);
        }

        System.out.println(hashTable.size);
        System.out.println(hashTable.deletedNotRemoved);
        System.out.println(hashTable.m);
    }
}