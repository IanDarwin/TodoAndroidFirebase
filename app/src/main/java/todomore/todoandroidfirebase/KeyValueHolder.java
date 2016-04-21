package todomore.todoandroidfirebase;

/**
 * Created by ian on 2016-04-21.
 */
public class KeyValueHolder<K,V> {
    K key;
    V value;

    public KeyValueHolder(K key, V value) {
        this.key = key;
        this.value = value;
    }
}
