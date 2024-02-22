package framework.lang.struct.dist;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

public class SkipNode<K extends Comparable, T>
{
    K key; // 按照此字段进行排序
    T value;
    SkipNode right,down;//右下个方向的指针
    public SkipNode (K key, T value) {
        this.key = key;
        this.value = value;
    }
}

public class SClassicalSkiplist<K extends Comparable, V> extends SSkipList<K, V>{

    SkipNode<K, V> headNode;//头节点，入口
    int highLevel;//当前跳表索引层数
    Random random;// 用于投掷硬币
    final int MAX_LEVEL = 32;//最大的层
    int count = 0;
    public SClassicalSkiplist(){
        random = new Random();
        // 头节点的key比较特殊，是int 最小值
        headNode = new SkipNode(Integer.MIN_VALUE, null);
        highLevel = 0;
    }
    @Override
    public boolean put(K key, V value) {
        SkipNode<K, V> node = new SkipNode<>(key, value);
        SkipNode<K, V> findNode = search(key);
        if(findNode != null)//如果存在这个key的节点
        {
            return false;
        }

        Stack<SkipNode> stack = new Stack<>();//存储向下的节点
        SkipNode team = headNode;//查找待插入的节点   找到最底层的哪个节点。
        while (team != null) {//进行查找操作
            if(team.right == null)//右侧没有了，只能下降
            {
                stack.add(team);//将曾经向下的节点记录一下
                team = team.down;
            }
            else if(team.right.key.compareTo(key) > 0)//需要下降去寻找
            {
                stack.add(team);//将曾经向下的节点记录一下
                team = team.down;
            }
            else //向右
            {
                team = team.right;
            }
        }

        int level = 1;//当前层数，从第一层添加(第一层必须添加，先添加再判断)
        SkipNode<K, V> downNode=null;//保持前驱节点(即down的指向，初始为null)
        while (!stack.isEmpty()) {
            //在该层插入node
            team = stack.pop(); //抛出待插入的左侧节点
            SkipNode nodeTeam = new SkipNode(node.key, node.value);//节点需要重新创建
            nodeTeam.down = downNode;//处理竖方向
            downNode = nodeTeam; //标记新的节点下次使用
            if(team.right == null) { //右侧为null 说明插入在末尾
                team.right = nodeTeam;
            }
            //水平方向处理
            else {//右侧还有节点，插入在两者之间
                nodeTeam.right = team.right;
                team.right = nodeTeam;
            }
            //考虑是否需要向上
            if(level > MAX_LEVEL)//已经到达最高级的节点啦
                break;
            double num = random.nextDouble();//[0-1]随机数
            if(num > 0.5) //运气不好结束
                break;
            level++;
            if(level > highLevel)//比当前最大高度要高但是依然在允许范围内 需要改变head节点
            {
                highLevel = level;
                //需要创建一个新的节点
                SkipNode highHeadNode = new SkipNode(Integer.MIN_VALUE, null);
                highHeadNode.down = headNode;
                headNode = highHeadNode;//改变head
                stack.add(headNode);//下次抛出head
            }
        }
        count++;
        return true;
    }



    @Override
    public List<SkipNode<K, V>> rangeSearch(K key1, K key2) {
        SkipNode<K, V> currentLayerLeft = headNode;
        SkipNode<K, V> currentLayerRight = headNode;
        List<SkipNode<K, V>> result = new ArrayList<>();

        if(headNode == null)
            return result;



        while(true){
            while(currentLayerLeft.right != null){
                if(currentLayerLeft.right.key.compareTo(key1) < 0){
                    currentLayerLeft = currentLayerLeft.right;
                }else {
                    break;
                }

            }
            currentLayerRight = currentLayerLeft;
            while(currentLayerRight.right != null){
                if(currentLayerRight.key.compareTo(key2) < 0){
                    currentLayerRight = currentLayerRight.right;

                }else{
                    break;
                }
            }
            if(currentLayerLeft.down != null && currentLayerRight.down != null){
                currentLayerLeft = currentLayerLeft.down;
                currentLayerRight = currentLayerRight.down;
            }else{
                break;
            }
        }

        while(currentLayerLeft != null){
            if(currentLayerLeft.key.compareTo(key1) >= 0 && currentLayerLeft.key.compareTo(key2) <= 0){
                result.add(currentLayerLeft);
                currentLayerLeft = currentLayerLeft.right;
            }else{
                break;
            }
        }

        return result;
    }

    @Override
    public boolean contains(V element) {
        SkipNode<K, V> headNode1 = headNode;
        while (headNode1.down != null){
            headNode1 = headNode1.down;
        }
        while (headNode1 != null){
            if(headNode1.value.equals(element)) return true;
            headNode1 = headNode1.right;
        }
        return false;
    }

    @Override
    public int size() {
        return count;
    }

    @Override
    public SkipNode<K, V> search(K key) {
        SkipNode<K, V> team= headNode;
        while (team != null) {
            if(team.key.compareTo(key) == 0)
            {
                return team;
            }
            else if(team.right == null) //右侧没有了，只能下降
            {
                team = team.down;
            }
            else if(team.right.key.compareTo(key) > 0)//需要下降去寻找
            {
                team = team.down;
            }
            else //右侧比较小向右
            {
                team = team.right;
            }
        }
        return null;
    }

    @Override
    public void delete(K key) {
        SkipNode<K, V> team = headNode;
        boolean deleted = false;
        while (team != null) {
            if (team.right == null) {// 右侧没有了，说明这一层找到，没有只能下降
                team = team.down;
            }
            else if(team.right.key == key) // 找到节点，右侧即为待删除节点
            {
                team.right = team.right.right; // 删除右侧节点
                deleted = true;
                team = team.down; // 向下继续查找删除
            }
            else if(team.right.key.compareTo(key) > 0) // 右侧已经不可能了，向下
            {
                team = team.down;
            }
            else { // 节点还在右侧
                team = team.right;
            }
        }
        if(deleted) count--;
    }


}
