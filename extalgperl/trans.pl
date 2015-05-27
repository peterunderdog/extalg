#!h:/usr/local/perl5/bin/perl
use strict refs;

##############################################################################
# rev: returns reversed string module, i.e., reverses direction and
# changes sense of the arrows; e.g. &rev("a<b<c>d") returns "d<c>b>a";
# both $a and &rev($a) represent the same string module
#
sub rev {
  my $a=join('',reverse(split(//,shift)));
  $a=~tr/<>/></;
  return $a;
}

##############################################################################
# splice: splices two stringmods along common vertex...
# e.g., &splice("...y>x", "x<z...")=...y>x<z...returns "" if 
# 'common' vertices are different, or if y==z.
#
sub splice {
  my ($len0)=length($_[0]);
  my ($len1)=length($_[1]);
  my ($err)="Attempted to splice misformed strings $_[0] and $_[1]";
  my ($x);
  my ($y);
  if ($len0==0 || $len1==0) {
    return "";
  }
  elsif (substr($_[0],$len0-1,1) ne substr($_[1],0,1)) {
    return "";
  }
  elsif ($len0==1) {
    return $_[1];
  }
  elsif ($len1==1) {
    return $_[0];
  }
  elsif ($len0==2 || $len1==2) {
    die $err;
  }
  elsif (substr($_[0],$len0-3,1) eq substr($_[1],2,1)) {
    return "";
  }
  else {
    return $_[0].substr($_[1],1);
  }
}

# splices list of paths into projective module...
# paths are assumed to start at same vertex...
# if list contains one element, it is returned,
# unchanged
sub splice_paths
{
	my ($p1, $p2)=@_;
	if ($p1 && $p2)
	{
  	return &splice(&rev($p1), $p2);
	}
	else
	{
		return $p1;
	}
}

# translatePath
# translates string into path, where the string is a
# comma-separated list of indices which point to arrows
# in the array referenced by $b
#
sub translatePath
{
	my ($b, $str)=@_;
	my @path=split /,/, $str;
	@path = map {$_=$$b[$_]} @path;
	my $p="";
	foreach my $arr (@path)
	{
		if ($p) {
			$p=&splice($p, $arr);
		}
		else {
			$p=$arr;
		}
	}
	return $p;
}

# translate string produced by quiverCAD applet into list
# of points (single character), arrows (denoted by a>b, where
# a,b are the endpoints of the arrow), zero relations
# (denoted as path of length > 1, e.g., a>b>c), and
# commutativity relations (denoted as a>b>c>d=a>x>y>d)...
# this data will be used to compute the projective modules
# for the algebra
sub translateQuiverString
{
	my ($q, $points, $arrows, $zerorel, $nupi)=@_;
  my @a=split /:/, $q;
	my @b;
  
  foreach my $s (@a)
  {
  	if ($s =~ /^P(.)/)
  	{
  		push @b, $1;
			push @$points, $1;
  	}
  	elsif ($s =~ /^A(.*)/)
  	{
  		my ($i, $j)=split /,/, $1;
			my ($start, $end)=($b[$i], $b[$j]);
			my ($arr)="$start>$end";
  		push @b, $arr;
			push @{$$arrows{$start}}, $arr;
  	}
  	elsif ($s =~ /^Z(.*)/)
  	{
			my $path=&translatePath(\@b, $1);
  		push @b, $path;
			push @$zerorel, $path;
  	}
  	elsif ($s =~ /^C(.*)/)
  	{
  		my (@paths)=split /;/, $1;
			@paths=map{&translatePath(\@b, $_)} @paths;
			# this is a placeholder more than anything...the
			# a>b>..>z=a>x>..>z string isn't used anywhere
			my $nupimod=&splice_paths(@paths);
			my $start=substr($paths[0], 0, 1);
  		push @b, $nupimod;
			# add non-uniserial proj/inj module to hash
			$$nupi{$start}=$nupimod;
			# add the paths to zero relations also...although these
			# paths are non-zero, any path strictly containing them
			# is zero, by the special biserial conditions:
			#     x-..-.
			#    /  __  \
			# a-b  /  \  z
			#    \ `->  /
			#     y-..-.
			#
			# since a>b>x or a>b>y must be zero, it follows that
			# a>b>x>...>z = a>b>y>...>z=0
			push @$zerorel, @paths;
  	}
  }
}

# extend path to maximal non-zero path
#
sub extend_to_max
{
	my ($path, $arrows, $zerore)=@_;
	my ($end)=substr($path,-1,1);
	if (exists $$arrows{$end})
	{
  	foreach my $arr (@{$$arrows{$end}})
  	{
  		my ($xpath)=&splice($path, $arr);
  		if ($xpath !~ /($zerore)/)
  		{
  			return extend_to_max($xpath, $arrows, $zerore);
  		}
  	}
	}
	return $path;
}

# constructs projective modules from string produced by
# quiverCAD applet
sub getProjectivesFromQuiver
{
  $q=shift;
  my (@points, %arrows, @zerorel, %nupi);
  &translateQuiverString($q, \@points, \%arrows, \@zerorel, \%nupi);
	my $zerore=join "|", @zerorel;
	foreach my $p (@points)
	{
		if (exists $nupi{$p})
		{
			# non-uniserial proj/inj
			$proj{$p}=$nupi{$p};
		}
		elsif (!exists $arrows{$p})
		{
			#no arrows out, so it's a simple projective
			$proj{$p}=$p;
		}
		else
		{
			my @maxpaths=map{&extend_to_max($_, \%arrows, $zerore)} @{$arrows{$p}};
			$proj{$p}=&splice_paths(@maxpaths);
		}
	}
}

&getProjectivesFromQuiver(shift);
