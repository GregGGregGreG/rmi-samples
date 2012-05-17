package one.rmi.serial;

import one.rmi.io.ByteArrayStream;

import java.io.IOException;
import java.util.ArrayList;

public class DeserializeStream extends ByteArrayStream {
    protected ArrayList<Object> context;
    
    public DeserializeStream(byte[] input) {
        super(input);
        this.context = new ArrayList<Object>(16);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object readObject() throws IOException, ClassNotFoundException {
        Serializer serializer;
        byte b = buf[count++];
        if (b >= 0) {
            count--;
            serializer = Repository.requestSerializer(readLong());
        } else if (b == SerializeStream.REF_NULL) {
            return null;
        } else if (b == SerializeStream.REF_RECURSIVE) {
            return context.get(readUnsignedShort());
        } else {
            serializer = Repository.requestBootstrapSerializer(b);
        }
        Object obj = serializer.read(this);
        context.add(obj);
        serializer.fill(obj, this);
        return obj;
    }

    @Override
    public long skip(long n) throws IOException {
        Serializer serializer;
        byte b = buf[count];
        if (b >= 0) {
            serializer = Repository.requestSerializer(readLong());
        } else if (b == -1) {
            count++;
            return 0;
        } else if (b == -2) {
            count += 3;
            return 0;
        } else {
            count++;
            serializer = Repository.requestBootstrapSerializer(b);
        }
        context.add(null);
        serializer.skip(this);
        return 0;
    }

    @Override
    public void close() {
        context = null;
    }
}
