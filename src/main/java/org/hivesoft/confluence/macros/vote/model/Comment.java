/*
 * Copyright (c) 2012, Confluence Community
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, 
 * with or without modification, are permitted provided 
 * that the following conditions are met:
 * 
 *     + Redistributions of source code must retain the above 
 *         copyright notice, this list of conditions and the following disclaimer.
 *     + Redistributions in binary form must reproduce the above copyright notice, 
 *         this list of conditions and the following disclaimer in the documentation 
 *         and/or other materials provided with the distribution.
 *     + Neither the name of Near Infinity Corporation nor the names of its contributors may 
 *         be used to endorse or promote products derived from this software without 
 *         specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS 
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.hivesoft.confluence.macros.vote.model;

/**
 * <p>
 * This class holds comments entered by users for a ballot.
 * </p>
 */
public class Comment {
	private String username;
	private String comment;

	/**
	 * <p>
	 * Create a new empty comment.
	 * </p>
	 */
	public Comment() {
	}

	/**
	 * <p>
	 * Create a new comment loaded with the given username and comment.
	 * </p>
	 * 
	 * @param username
	 *            The user who entered the comment.
	 * @param comment
	 *            The comment.
	 */
	public Comment(String username, String comment) {
		this.username = username;
		this.comment = comment;
	}

	/**
	 * <p>
	 * Get the text of this comment.
	 * </p>
	 * 
	 * @return The comment as a <code>String</code>
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * <p>
	 * Set the text of this comment.
	 * </p>
	 * 
	 * @param comment
	 *            The comment as a <code>String</code>
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * <p>
	 * Get the name of the user that entered this comment.
	 * </p>
	 * 
	 * @return The username for this comment.
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * <p>
	 * Set the name of the user that entered this comment.
	 * </p>
	 * 
	 * @param username
	 *            The username for this comment.
	 */
	public void setUsername(String username) {
		this.username = username;
	}
}
