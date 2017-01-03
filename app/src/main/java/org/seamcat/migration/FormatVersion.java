package org.seamcat.migration;

import java.util.ArrayList;
import java.util.List;


/** Represents versions of some file storage format.
 * The format versions are numbered sequentially.
 */
public final class FormatVersion {

	private int number;
	
	public FormatVersion(int number) {
		this.number = number;
	}
	
	public int getNumber() {
		return number;
	}

	public boolean isLaterThan(FormatVersion other) {
      return number > other.number;
   }

	public static List<FormatVersion> rangeOf(FormatVersion start, FormatVersion end) {
   	List<FormatVersion> versions = new ArrayList<FormatVersion>();
   	for (int i=start.number; i<=end.number; i++) {
   		versions.add(new FormatVersion(i));
   	}
      return versions;
   }
	
	public FormatVersion nextVersion() {
	   return new FormatVersion(number+1);
   }

	@Override
   public int hashCode() {
	   final int prime = 31;
	   int result = 1;
	   result = prime * result + number;
	   return result;
   }

	@Override
   public boolean equals(Object obj) {
	   if (this == obj)
		   return true;
	   if (obj == null)
		   return false;
	   if (getClass() != obj.getClass())
		   return false;
	   FormatVersion other = (FormatVersion) obj;
	   if (number != other.number)
		   return false;
	   return true;
   }		

	@Override
   public String toString() {
	   return "v" + number;
   }
}
