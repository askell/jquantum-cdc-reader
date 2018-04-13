package com.askell;

import de.ibapl.spsw.api.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.ServiceLoader;

class QuantumSocket {

    private static final byte[] HEADER = {(byte) 0xC0, (byte) 0x00, (byte) 0xE1, (byte) 0xF0, (byte) 0xC0};
    private static final byte[] READ_COMMAND = {(byte) 0xC0, (byte) 0xDB, (byte) 0xDC, (byte) 0x20, (byte) 0x2F, (byte) 0x39, (byte) 0xC0};

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Please provide port name");
            return;
        }
        ServiceLoader<SerialPortSocketFactory> loader = ServiceLoader.load(SerialPortSocketFactory.class);
        Iterator<SerialPortSocketFactory> iterator = loader.iterator();
        SerialPortSocketFactory serialPortSocketFactory = iterator.next();
        try (SerialPortSocket serialPortSocket = serialPortSocketFactory.open(args[0], Speed._9600_BPS, DataBits.DB_8, StopBits.SB_1, Parity.NONE, FlowControl.getFC_NONE())) {
            serialPortSocket.setXONChar((char) 0x00);
            serialPortSocket.setXOFFChar((char) 0x00);

            serialPortSocket.getOutputStream().write(HEADER);
            Thread.sleep(serialPortSocket.calculateMillisForCharacters(HEADER.length));
            serialPortSocket.getOutputStream().write(READ_COMMAND);

            final byte[] response = new byte[32];
            int readBytes = serialPortSocket.getInputStream().read(response);
            if (readBytes != 32) {
                System.err.println("Invalid number of bytes read: " + readBytes);
                return;
            }
            int leftValue = getValue(response, 9);
            int rightValue = getValue(response, 13);
            int accumulatedAverage = getValue(response, 25);

            System.out.println("current left=" + leftValue);
            System.out.println("current right=" + rightValue);
            System.out.println("accumulated average=" + accumulatedAverage);
        } catch (IOException | InterruptedException ioe) {
            System.err.println(ioe);
        }
    }

    private static int getValue(byte[] response, int start) {
        ByteBuffer wrapped = ByteBuffer.wrap(response, start, 4);
        return wrapped.getInt();
    }

}
