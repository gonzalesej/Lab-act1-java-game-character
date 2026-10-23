import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Abstract Data Type: a singly linked list of GameCharacter objects.
 * Users of this class only see add/remove/find/search/replace.
 * How the nodes are linked is hidden inside.
 */
public class CharacterList implements Iterable<GameCharacter> {

    private static class Node {
        GameCharacter data;
        Node next;
        Node(GameCharacter data) { this.data = data; }
    }

    private Node head;
    private Node tail;
    private int size;

    public void add(GameCharacter c) {
        Node node = new Node(c);
        if (head == null) {
            head = node;
        } else {
            tail.next = node;
        }
        tail = node;
        size++;
    }

    public boolean remove(GameCharacter target) {
        Node prev = null;
        Node cur = head;
        while (cur != null) {
            if (cur.data == target) {
                if (prev == null) head = cur.next;
                else prev.next = cur.next;
                if (cur == tail) tail = prev;
                size--;
                return true;
            }
            prev = cur;
            cur = cur.next;
        }
        return false;
    }

    // Swap one character for another, keeping its position in the list
    public boolean replace(GameCharacter oldChar, GameCharacter newChar) {
        for (Node cur = head; cur != null; cur = cur.next) {
            if (cur.data == oldChar) {
                cur.data = newChar;
                return true;
            }
        }
        return false;
    }

    public GameCharacter findByName(String name) {
        for (GameCharacter c : this) {
            if (c.getName().equalsIgnoreCase(name)) return c;
        }
        return null;
    }

    // Returns a new list with characters whose name or type contains the keyword
    public CharacterList search(String keyword) {
        CharacterList result = new CharacterList();
        String k = (keyword == null) ? "" : keyword.toLowerCase();
        for (GameCharacter c : this) {
            if (k.isEmpty()
                    || c.getName().toLowerCase().contains(k)
                    || c.getType().toLowerCase().contains(k)) {
                result.add(c);
            }
        }
        return result;
    }

    public int size() { return size; }

    public boolean isEmpty() { return size == 0; }

    @Override
    public Iterator<GameCharacter> iterator() {
        return new Iterator<GameCharacter>() {
            private Node current = head;

            @Override
            public boolean hasNext() { return current != null; }

            @Override
            public GameCharacter next() {
                if (current == null) throw new NoSuchElementException();
                GameCharacter c = current.data;
                current = current.next;
                return c;
            }
        };
    }
}