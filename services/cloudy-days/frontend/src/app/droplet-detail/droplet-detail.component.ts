import {Component, Input, OnInit} from '@angular/core';
import {Droplet} from "../droplet";
import {DropletService} from "../droplet.service";
import {ActivatedRoute} from "@angular/router";

@Component({
  selector: 'app-droplet-detail',
  templateUrl: './droplet-detail.component.html',
  styleUrls: ['./droplet-detail.component.css']
})
export class DropletDetailComponent implements OnInit {
  @Input() droplet?: Droplet;
  args: string[] = [];
  result?: string;

  constructor(private route: ActivatedRoute, private dropletService: DropletService) { }

  ngOnInit(): void {
    this.getDroplet();
  }

  getDroplet(): void {
    const name = this.route.snapshot.paramMap.get('name');
    if (name) {
      this.dropletService.getOne(name).subscribe(droplet => this.droplet = droplet);
    }
  }

  onChange(): void {
    this.result = "";
  }

  onExecute(): void {
    if (this.droplet) {
      this.dropletService.execute(this.droplet, this.args).subscribe(result => this.result = result);
    }
  }

  onAddArgument(): void {
    this.args.push("");
  }

  onRemoveArgument(): void {
    this.args.pop();
  }

  trackByFn(index: number, item: any) {
    return index;
  }
}
