/*
 * Apache 2.0 License
 *
 * Copyright (c) 2019 Intuit Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.intuit.cloudraider.model;

/**
 * The enum Actions.
 */
public enum Actions {

    /**
     * Spikecpu actions.
     */
    SPIKECPU("spikecpu"),
    /**
     * Stressdisk actions.
     */
    STRESSDISK("stressdisk"),
    /**
     * Corruptnetwork actions.
     */
    CORRUPTNETWORK("networkcorruption"),
    /**
     * Delaynetwork actions.
     */
    DELAYNETWORK("networklatency"),
    /**
     * Delaydomainnetwork actions.
     */
    DELAYDOMAINNETWORK("domainnetworklatency"),
    /**
     * Packetloss actions.
     */
    PACKETLOSS("networkpacketloss"),
    /**
     * Killjavaprocess actions.
     */
    KILLJAVAPROCESS("killjavaprocess"),
    /**
     * List actions.
     */
    LIST("list"),
    /**
     * Killprocess actions.
     */
    KILLPROCESS("killprocess"),
    /**
     * Diskfull actions.
     */
    DISKFULL("diskfull"),
    /**
     * Stopservice actions.
     */
    STOPSERVICE("stopservice"),
    /**
     * Startservice actions.
     */
    STARTSERVICE("startservice"),
    /**
     * Blockdomain actions.
     */
    BLOCKDOMAIN("blockdomain"),
    /**
     * Blockport actions.
     */
    BLOCKPORT("blockport"),
    /**
     * Unblockport actions.
     */
    UNBLOCKPORT("unblockport"),
    /**
     * Unblockdomain actions.
     */
    UNBLOCKDOMAIN("unblockdomain"),
    /**
     * Blockdynamo actions.
     */
    BLOCKDYNAMO("blockdynamo"),
    /**
     * Unblockdynamo actions.
     */
    UNBLOCKDYNAMO("unblockdynamo"),
    /**
     * Blocks 3 actions.
     */
    BLOCKS3("blocks3"),
    /**
     * Unblocks 3 actions.
     */
    UNBLOCKS3("unblocks3"),
    /**
     * Renamefile actions.
     */
    RENAMEFILE("renamefile"),
    /**
     * Blocktargetnetwork actions.
     */
    BLOCKTARGETNETWORK("blocktargetnetwork"),
    /**
     * Unblocktargetnetwork actions.
     */
    UNBLOCKTARGETNETWORK("unblocktargetnetwork"),
    /**
     * Delaytargetnetwork actions.
     */
    DELAYTARGETNETWORK("delaytargetnetwork"),
    /**
     * Removedelaytargetnetwork actions.
     */
    REMOVEDELAYTARGETNETWORK("removedelaytargetnetwork");

    private final String actionString;

    private Actions(String actionString) {
        this.actionString = actionString;
    }

    /**
     * Gets action name.
     *
     * @return the action name
     */
    public String getActionName() {
        return this.actionString;
    }
}
