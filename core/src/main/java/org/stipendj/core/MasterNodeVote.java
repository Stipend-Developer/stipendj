/**
 * Copyright 2014 Hash Engineering Solutions
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.stipendj.core;

import org.stipendj.script.Script;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

import static org.stipendj.core.Utils.int64ToByteStreamLE;
import static org.stipendj.core.Utils.uint32ToByteStreamLE;

public class MasterNodeVote  extends ChildMessage implements Serializable {
    public int votes;
    public Script pubkey;
    int version;
    boolean setPubkey;

    long blockHeight;

    static final int CURRENT_VERSION=1;

    private transient int optimalEncodingMessageSize;

    MasterNodeVote()
    {
        version = CURRENT_VERSION;
        votes = 0;
        pubkey = null;
        blockHeight = 0;
    }
    /*MasterNodeVote(NetworkParameters params, byte [] bytes, int cursor, Message parent, boolean parseLazy, boolean parseRetain, int length)
    {
        super(params, bytes, cursor, parent, parseLazy, parseRetain, length);
    }*/


    protected static int calcLength(byte[] buf, int offset) {
        VarInt varint;
        // jump past version (uint32)
        int cursor = offset;// + 4;
        // jump past the block heignt (uint64)
        cursor += 8;

        int i;
        long scriptLen;

        varint = new VarInt(buf, cursor);
        long sizeScript = varint.value;
        cursor += varint.getOriginalSizeInBytes();
        cursor += sizeScript;


        // 4 = length of number votes (uint32)
        return cursor - offset + 4;
    }
    @Override
    protected void parse() throws ProtocolException {

        cursor = offset;
        version = CURRENT_VERSION;

        blockHeight = readInt64();
        optimalEncodingMessageSize = 8;


        long scriptSize = readVarInt();
        optimalEncodingMessageSize += VarInt.sizeOf(scriptSize);
        byte [] scriptBytes = readBytes((int)scriptSize);
        pubkey = new Script(scriptBytes);
        optimalEncodingMessageSize += scriptSize;

        votes = (int)readUint32();

        optimalEncodingMessageSize += 4;

        length = cursor - offset;


    }
    @Override
    protected void bitcoinSerializeToStream(OutputStream stream) throws IOException {
        //uint32ToByteStreamLE(version, stream);
        int64ToByteStreamLE(blockHeight, stream);
        //scrypt pubkey         //TODO: not finished
        byte [] scriptBytes = pubkey.getProgram();
        stream.write(new VarInt(scriptBytes.length).encode());
        stream.write(scriptBytes);
        //this.
        uint32ToByteStreamLE(votes, stream);
    }

    long getOptimalEncodingMessageSize()
    {
        if(optimalEncodingMessageSize != 0)
            return optimalEncodingMessageSize;

        //TODO: not finished
        //version
        //optimalEncodingMessageSize = 4;
        //block height
        optimalEncodingMessageSize += 8;
        //pubkey
        byte [] scriptBytes = pubkey.getProgram();

        optimalEncodingMessageSize += VarInt.sizeOf(scriptBytes.length);
        optimalEncodingMessageSize += scriptBytes.length;
        //votes
        optimalEncodingMessageSize += 4;

        return optimalEncodingMessageSize;
    }

    public String toString()
    {
        return "Master Node Vote: v" + version + "; blockHeight " + blockHeight + "; pubkey" + pubkey.toString() +  "; votes: " + votes + "\n";
    }


    public void vote()
    {
        votes++;
    }
    public int getVotes()
    {return votes;}

    public long getHeight()
    {return blockHeight;}

    Script getPubkey() { return pubkey;}
}
