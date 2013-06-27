Introduction
============

Welcome to the Confluence-Survey-Plugin Page.

Usage
=====

The following usage is mainly copied from the shipped help (within the plugin)

Survey-Macro
------------

<table>
<tr bgcolor=ffffff>
  <td>
    {survey:changeableVotes=true|voters=user1,user2|viewers=user3}<br />
    Knowledge - This is the knowledge category.<br />
    Communication - This is the communication category.<br />
    {survey}<br />
  </td>

  <td>
    <p>
      The survey macro allows Confluence users to be surveyed on several categories.
      For each category, users are allowed to select only one of the given choices,
      and the results will not be visible to them until they have voted. Users
      that have not logged in will be prompted to do so before allowing them to
      cast a vote. This macro was created to support surveys of confluence users
      on several categories and will provide them with the chance to give a
      rating (1 to 5) for each category as well as a comment.
    </p>
    <p>
      The body of this macro defines the categories that the users will be polled
      on. Each line of the body will be treated as a seperate category and
      should be written in the format "title - description". The title is
      always required but the dash and the description are optional.
    </p>

    <p>
    <!-- Provide a list of all possible macro arguments -->
    <table>
      <tr>
        <th>Parameter</th>
        <th>Required</th>
        <th>Default</th>
        <th>Description</th>
      </tr>
      <tr>
        <td>title</td>
        <td>false</td>
        <td>default no title</td>
        <td>If a Title is specified the Survey gets a Box around which makes it looking more compact
            and feeling the votes are belonging more together.
        </td>
      </tr>
      <tr>
        <td>voters</td>
        <td>false</td>
        <td>all users</td>
        <td>This is a comma seperated list of usernames to who are allowed to
            cast a vote. Users not in this list will not be allowed to vote, but
            if they are viewers will be shown the results of the vote. If this parameter
            is not specified, all users with access to the page are considered voters.
        </td>
      </tr>
      <tr>
        <td>viewers</td>
        <td>false</td>
        <td>all users</td>
        <td>This is a comma seperated list of usernames to who are allowed to see the survey results.
            Users not in this list will be allowed to vote but
            after doing so will simply be shown which item they voted for. If a user is in this
            list but is not a voter, they will be taken straight to the results. If this parameter is not specified,
            all users will be able to see the results.
        </td>
      </tr>
      <tr>
        <td>changeableVotes</td>
        <td>false</td>
        <td>false</td>
        <td>This parameter, if set to true, will allow the users to change their responses
            after they have been cast.
        </td>
      </tr>
      <tr>
	<td>choices</td>
	<td>false</td>
	<td>default 1-5</td>
	<td>A comma separated List of choices. This will override the Default (1-5) List, but can still be overriden by
	    a '-' separated list in each single line.
	</td>
      </tr>
      <tr>
      	<td>showComments</td>
      	<td>false</td>
      	<td>true</td>
      	<td>Show comments-menu (the whole set: show, add, edit, delete)</td>
      </tr>
      <tr>
	<td>locked</td>
	<td>false</td>
	<td>false</td>
	<td>Dont allow any further voting. Show a lock Symbol to indicate that.
	    Image for Survey will only be displayed if you have the title-flag also. (It is still shown on the vote-elements)</td>
      </tr>
    </table>
    </p>
    </td>
</tr>
</table>


Vote-Macro
==========

<table>
<tr bgcolor=ffffff>
  <td>
    {vote:What is your favorite color?}<br />
    Red<br />
    Blue<br />
    None of the above<br />
    {vote}<br />
    <br />
    {vote:What is your favorite color?|changeableVotes=true|voters=user1,user2}<br />
    Red<br />
    Blue<br />
    None of the above<br />
    {vote}<br />
  </td>

  <td>
    <p>
      The vote macro allows Confluence users to vote on any topic of interest.
      Users are allowed to select only one of the given choices and vote one time,
      and the results will not be visible to them until they have voted. Users
      that have not logged in will be prompted to do so before allowing them to
      cast a vote. This macro was created to support quick, informal votes on
      various topics. The macro has a title and a series of choices, each choice
      starting on its own line.

    <!-- Provide a list of all possible macro arguments -->
    <table>
      <tr>
        <th>Parameter</th>
        <th>Required</th>
        <th>Default</th>
        <th>Description</th>
      </tr>
      <tr>
        <td><br /></td>
        <td>true</td>
        <td><br /></td>
        <td>This is the title of the ballot and must be the first paramter.</td>
      </tr>
      <tr>
        <td>voters</td>
        <td>false</td>
        <td>all users</td>
        <td>This is a comma seperated list of usernames to who are allowed to
            cast a vote. Users not in this list will not be allowed to vote, but
            if they are viewers will be shown the results of the vote. If this parameter
            is not specified, all users with access to the page are considered voters.
        </td>
      </tr>
      <tr>
        <td>viewers</td>
        <td>false</td>
        <td>all users</td>
        <td>This is a comma seperated list of usernames to who are allowed to see the survey results.
            Users not in this list will be allowed to vote but
            after doing so will simply be shown which item they voted for. If a user is in this
            list but is not a voter, they will be taken straight to the results. If this parameter is not specified,
            all users will be able to see the results.
        </td>
      </tr>
      <tr>
        <td>changeableVotes</td>
        <td>false</td>
        <td>false</td>
        <td>This parameter, if set to true, will allow the voters to change their vote
            after it has been cast.
        </td>
      </tr>
      <tr>
      	<td>locked</td>
      	<td>false</td>
      	<td>false</td>
      	<td>Dont allow any further voting. Show a lock Symbol to indicate that.</td>
      </tr>
    </table>
    </p>

    <p>Before the user logs in:</p>
    <div align='center'>
    <table class="grid">
      <tr>
        <th colspan="2">What is your favorite color?  <span class="smalltext">(<a href="#">Log In</a> to vote.)</span></th>
      </tr>
      <tr>
        <th>Choices</th>
        <th>Your Vote</th>
      </tr>
      <tr>
        <td>Red</td>
        <td align="center">
          <img src="$req.contextPath/download/resources/${project.groupId}.${project.artifactId}/gray-yes.gif" border="0" align="absmiddle" height="17" width="17" name="vote">
        </td>
      </tr>
      <tr>
        <td>Blue</td>
        <td align="center">
          <img src="$req.contextPath/download/resources/${project.groupId}.${project.artifactId}/gray-yes.gif" border="0" align="absmiddle" height="17" width="17" name="vote">
        </td>
      </tr>
      <tr>
        <td>None of the above</td>
        <td align="center">
          <img src="$req.contextPath/download/resources/${project.groupId}.${project.artifactId}/gray-yes.gif" border="0" align="absmiddle" height="17" width="17" name="vote">
        </td>
      </tr>
    </table>
    </div>

    <p>Before the logged-in user votes:</p>
    <div align='center'>
    <table class="grid">
      <tr>
        <th colspan="2">What is your favorite color?</th>
      </tr>
      <tr>
        <th>Choices</th>
        <th>Your Vote</th>
      </tr>
      <tr>
        <td>Red</td>
        <td align="center">
          <img src="$req.contextPath/download/resources/${project.groupId}.${project.artifactId}/blue-yes.gif" border="0" align="absmiddle" height="17" width="17" name="vote">
        </td>
      </tr>
      <tr>
        <td>Blue</td>
        <td align="center">
          <img src="$req.contextPath/download/resources/${project.groupId}.${project.artifactId}/blue-yes.gif" border="0" align="absmiddle" height="17" width="17" name="vote">
        </td>
      </tr>
      <tr>
        <td>None of the above</td>
        <td align="center">
          <img src="$req.contextPath/download/resources/${project.groupId}.${project.artifactId}/blue-yes.gif" border="0" align="absmiddle" height="17" width="17" name="vote">
        </td>
      </tr>
    </table>
    </div>

    <p>After the logged-in user votes:</p>
    <div align='center'>
    <table class="grid">
      <tr>
        <th colspan="3">What is your favorite color?</th>
      </tr>
      <tr>
        <th>Choices</th>
        <th>Your Vote</th>
        <th>Current Results: (10 total votes)</th>
      </tr>
      <tr>
        <td>Red</td>
        <td align="center">
          <img src="$req.contextPath/download/resources/${project.groupId}.${project.artifactId}/green-yes.gif" border="0" align="absmiddle" height="17" width="17" name="vote">
        </td>
        <td>
          <table width="100%" cellpadding="0" cellspacing="0" border="0">
            <tr>
              <td height="8" width="40%" class="greenbar"></td>
              <td height="8" width="60%" class="greenbar" style="BACKGROUND: #d0d0d0;"></td>
            </tr>
            <tr>
              <td colspan="2" height="12" valign="absmiddle" align="center"><span class="smalltext">(4 votes, 40%)</span></td>
            </tr>
          </table>
        </td>
      </tr>
      <tr>
        <td>Blue</td>
        <td align="center">
          <img src="$req.contextPath/download/resources/${project.groupId}.${project.artifactId}/gray-yes.gif" border="0" align="absmiddle" height="17" width="17" name="vote">
        </td>
        <td>
          <table width="100%" cellpadding="0" cellspacing="0" border="0">
            <tr>
              <td height="8" width="50%" class="greenbar"></td>
              <td height="8" width="50%" class="greenbar" style="BACKGROUND: #d0d0d0;"></td>
            </tr>
            <tr>
              <td colspan="2" height="12" valign="absmiddle" align="center"><span class="smalltext">(5 votes, 50%)</span></td>
            </tr>
          </table>
        </td>
      </tr>
      <tr>
        <td>None of the above</td>
        <td align="center">
          <img src="$req.contextPath/download/resources/${project.groupId}.${project.artifactId}/gray-yes.gif" border="0" align="absmiddle" height="17" width="17" name="vote">
        </td>
        <td>
          <table width="100%" cellpadding="0" cellspacing="0" border="0">
            <tr>
              <td height="8" width="10%" class="greenbar"></td>
              <td height="8" width="90%" class="greenbar" style="BACKGROUND: #d0d0d0;"></td>
            </tr>
            <tr>
              <td colspan="2" height="12" valign="absmiddle" align="center"><span class="smalltext">(1 votes, 10%)</span></td>
            </tr>
          </table>
        </td>
      </tr>
    </table>
    </div>
    </td>
</tr>
</table>

History
=======

The Plugin originated in 2006 and was taken over for maintenance in 2008, that time it was quite unsupported.

I maintained it until i had no personel access to a confluence installation anymore (2011).

As in 2012 the JIRA Studio at Atlassian closed its doors i had to find another location. Welcome GitHub ;)
In June 2013 i had quite some inquiries to carry on, so i am currently evaluating some things (compatibility, documentation, maybe features).

Main focus is maintenance, test coverage and getting the thing somehow usable with the macro browser.

If someone feels to help out, feel free to contact me.