package org.seamcat.testutil;

import junit.framework.Assert;

import org.apache.commons.jxpath.JXPathContext;


public class XPathAssert {
	
	JXPathContext context;
	
   public XPathAssert(Object contextObject) {
   	context = JXPathContext.newContext(contextObject);
   }

	public void nodeValueEquals(Object expectedValue, String xpath) {
		Object actualValue = context.getValue(xpath);
		Assert.assertEquals(expectedValue, actualValue);
	}

	public void hasOneNode(String xpath) {
	   int matchingNodeCount = context.selectNodes(xpath).size();
		if (matchingNodeCount != 1) {
	   	Assert.fail("Expected exactly one node to match " + xpath + ", but found " + matchingNodeCount);
	   }	   
   }
	
	public void hasNoNode( String xpath ) {
		int matchingNodeCount = context.selectNodes(xpath).size();
		if (matchingNodeCount != 0) {
			Assert.fail("Expected no nodes to match " + xpath + ", but found " + matchingNodeCount);
		}
	}
}
