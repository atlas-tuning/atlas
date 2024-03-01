package com.github.manevolent.atlas.ssm4;

import com.github.manevolent.atlas.Frame;

import javax.crypto.Cipher;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.GeneralSecurityException;
import java.util.Arrays;

public class FlashWriteKeyScanner {

    /**
     * FlashWrite.exe is a tool used to write flash data to Subaru ECUs.
     *
     * Keys for the UDS protocol are not encrypted at rest.
     * You can literally just brute force keys using this tool.
     *
     * You need to have intercepted FlashWrite or a similar flashing tool on the wire (CAN bus) and
     * have already extracted the desired UDS SecurityAccess requests/responses.
     *
     * This tool will help you to know which keys were used for the SecurityAccess commands, so you
     * can then replicate those actions in your own tooling.
     *
     * FlashWrite2/FlashWrite are copyright programs and you do need to have your own copy to use this.
     *
     * @param args pass in the FlashWrite.exe you have as the first and only argument
     *             second argument is the UDS challenge to try and solve
     *             third argument is the already known solution to the challenge
     * @throws FileNotFoundException
     */
    public static void main(String[] args) throws IOException, GeneralSecurityException {
        long flashWriteExeLength = new File(args[0]).length();
        RandomAccessFile flashWriteExe = new RandomAccessFile(args[0], "r");

        // Describe a known challenge and solution (UDS)
        byte[] challenge = Crypto.toByteArray(args[1].replace(" ", ""));
        byte[] solution = Crypto.toByteArray(args[2].replace(" ", ""));

        if (challenge.length != solution.length) {
            throw new IllegalArgumentException("Challenge and solution lengths aren't " +
                    "the same. Is that right?");
        } else if (solution.length != 16) {
            throw new IllegalArgumentException("We expect AES-128 cipher-text for " +
                    "the solution, which is 16 bytes long");
        }

        long offs = 0;
        int keySize = solution.length;
        byte[] possibleKey = new byte[keySize];
        while (offs < flashWriteExeLength - keySize) {
            flashWriteExe.seek(offs);
            flashWriteExe.read(possibleKey);

            Cipher attemptCipher = Crypto.createCipher(Cipher.ENCRYPT_MODE, possibleKey, null);
            byte[] attemptSolution = attemptCipher.update(challenge);

            if (Arrays.equals(attemptSolution, solution)) {
                System.out.println("Solved!");
                System.out.println("Key at offset " + offs + " in program:");
                System.out.println(Frame.toHexString(possibleKey));
                break;
            }

            offs++;
        }
    }

}
