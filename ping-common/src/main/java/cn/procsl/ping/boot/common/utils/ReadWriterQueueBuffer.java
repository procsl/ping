package cn.procsl.ping.boot.common.utils;

import java.util.ArrayList;

public class ReadWriterQueueBuffer {

    private final ArrayList<Node> nodes = new ArrayList<>();
    private final int capacitySize = 1024;


    private Node getOrCreateLastNode() {
        if (nodes.isEmpty()) {
            this.nodes.addLast(new Node(capacitySize));
        }

        Node last = this.nodes.getLast();
        if (last.isFull()) {
            this.nodes.addLast(new Node(capacitySize));
        }

        return this.nodes.getLast();
    }

    /**
     * 追加写入
     *
     * @param data   原始数据
     * @param offset 偏移
     * @param length 写入长度
     */
    public void append(byte[] data, int offset, int length) {

        // 先校验参数
        if (data == null || offset + length > data.length) {
            throw new IllegalArgumentException("data or offset or length is invalid");
        }

        do {
            Node node = this.getOrCreateLastNode();
            int current = node.full(data, offset, length);
            offset += current;
            length -= current;
        } while (length > 0);
    }

    /**
     * 读取
     *
     * @param data
     * @param offset
     * @param length
     */
    public void dequeue(byte[] data, int offset, int length) {

    }

    private static class Node {
        private final byte[] data;
        private int offset;

        protected Node(int capacity) {
            if (capacity <= 99) {
                throw new IllegalArgumentException("capacity must be greater than 100");
            }
            this.data = new byte[capacity];
            this.offset = 0;
        }

        /**
         * 填充当前节点, 并返回写入的数量
         */
        protected int full(byte[] data, int offset, int length) {

            // 计算可写入数据量
            int writeableLength = this.getWriteLength();

            // 需要写入的量
            int writeLength = length - offset;

            // 如果可写入的数据量大于写入的数据量
            if (writeableLength >= writeLength) {
                System.arraycopy(data, offset, this.data, this.offset, writeLength);
                this.offset += writeLength;
                return writeLength;
            }

            // 否则, 写入可写入的数据量
            System.arraycopy(data, offset, this.data, this.offset, writeableLength);
            this.offset += writeableLength;
            return writeableLength;
        }

        protected int getWriteLength() {
            return this.data.length - offset;
        }

        protected boolean isFull() {
            return offset >= data.length;
        }

        protected boolean isEmpty() {
            return offset <= 0;
        }

        protected int size() {
            return offset;
        }

    }
}
