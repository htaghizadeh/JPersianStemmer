package global.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author htaghizadeh
 */

public class TrieImpl<T> implements TrieInterface<T> {
    private TrieNode<T> rootNode = new TrieNode<T>();
     
    @Override
    public void add(String key, T value) {
        addNode(rootNode, key, 0, value);
    }
 
    @Override
    public T find(String key) {
        return findKey(rootNode, key);
    }
 
    @Override
    public List<T> search(String prefix) {
        List<T> list = new ArrayList<T>();
         
        char[] ch = prefix.toCharArray();
        TrieNode<T> node = rootNode;
        for (int i = 0; i < ch.length; i++) {
            node = node.getChildren().get(ch[i]);
            if (node == null) {
                break;
            }
        }
         
        if (node != null) {
            getValuesFromNode(node, list);
        }
         
        return list;
    }
 
    @Override
    public boolean contains(String key) {
        return hasKey(rootNode, key);
    }
     
    @Override
    public Set<String> getAllKeys() {
        Set<String> keySet = new HashSet<String>();
        getKeysFromNode(rootNode, "", keySet);
         
        return keySet;
    }
     
    @Override
    public int size() {
        return getAllKeys().size();
    }
     
    private void getValuesFromNode(TrieNode<T> currNode, List<T> valueList) {
        if (currNode.isTerminal()) {
            valueList.add(currNode.getNodeValue());
        }
         
        Map<Character, TrieNode<T>> children = currNode.getChildren();
        Iterator childIter = children.keySet().iterator();
        while (childIter.hasNext()) {
            Character ch = (Character)childIter.next();
            TrieNode<T> nextNode = children.get(ch);
            getValuesFromNode(nextNode, valueList);
        }
    }
     
    private void getKeysFromNode(TrieNode<T> currNode, String key, Set keySet) {
        if (currNode.isTerminal()) {
            keySet.add(key);
        }
         
        Map<Character, TrieNode<T>> children = currNode.getChildren();
        Iterator childIter = children.keySet().iterator();
        while (childIter.hasNext()) {
            Character ch = (Character)childIter.next();
            TrieNode<T> nextNode = children.get(ch);
            String s = key + nextNode.getNodeKey();
            getKeysFromNode(nextNode, s, keySet);
        }
    }
     
    private T findKey(TrieNode<T> currNode, String key) {
        Character c = key.charAt(0);
        if (currNode.getChildren().containsKey(c)) {
            TrieNode<T> nextNode = currNode.getChildren().get(c);
            if (key.length() == 1) {
                if (nextNode.isTerminal()) {
                    return nextNode.getNodeValue();
                }
            } else {
                return findKey(nextNode, key.substring(1));
            }
        }
         
        return null;
    }
     
    private boolean hasKey(TrieNode<T> currNode, String key) {
        Character c = key.charAt(0);
        if (currNode.getChildren().containsKey(c)) {
            TrieNode<T> nextNode = currNode.getChildren().get(c);
            if (key.length() == 1) {
                if (nextNode.isTerminal()) {
                    return true;
                }
            } else {
                return hasKey(nextNode, key.substring(1));
            }
        }
         
        return false;
    }
     
    private void addNode(TrieNode<T> currNode, String key, int pos, T value) {
        Character c = key.charAt(pos);
        TrieNode<T> nextNode = currNode.getChildren().get(c);
         
        if (nextNode == null) {
            nextNode = new TrieNode<T>();
            nextNode.setNodeKey(c);
            if (pos < key.length() - 1) {
                addNode(nextNode, key, pos + 1, value);
            } else {
                nextNode.setNodeValue(value);
                nextNode.setTerminal(true);
            }
            currNode.getChildren().put(c, nextNode);
        } else {
            if (pos < key.length() - 1) {
                addNode(nextNode, key, pos + 1, value);
            } else {
                nextNode.setNodeValue(value);
                nextNode.setTerminal(true);
            }
        }
    }
}