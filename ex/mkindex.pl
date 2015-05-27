#!H:/usr/local/perl5/bin/perl
for $file (@ARGV) {
  open (F, "$file");
  print "<form action=\"extalg.pl\">\n";
  while (<F>) {
  	chomp;
  	print "<input type=hidden name=proj value=\"$_\">\n";	
  }
  print "<input type=submit value=\"$file\">\n";
  print "</form>\n\n";
}
